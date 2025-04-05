import { useState, useEffect } from "react";

interface UrlDto {
  url: string;
}

interface TokenDto {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
}

interface SavedToken {
  id: number;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  createdAt: string;
}

interface RefreshStatusState {
  [key: number]: "loading" | "success" | "error" | null;
}

function App() {
  const [tokens, setTokens] = useState<SavedToken[]>([]);
  const [refreshStatus, setRefreshStatus] = useState<RefreshStatusState>({});

  useEffect(() => {
    // Load and migrate tokens on component mount
    const migratedTokens = migrateExistingTokens();
    setTokens(migratedTokens);
  }, []);

  // Function to fix and migrate existing tokens
  function migrateExistingTokens(): SavedToken[] {
    try {
      const savedTokens = JSON.parse(
        localStorage.getItem("oauth2Tokens") ?? "[]"
      );

      const migratedTokens = savedTokens.map((token: Partial<SavedToken>) => {
        // Ensure all required fields exist with sensible defaults
        const validatedToken: SavedToken = {
          id: token.id ?? Date.now(),
          accessToken: token.accessToken ?? "",
          refreshToken: token.refreshToken ?? "",
          expiresIn:
            typeof token.expiresIn === "number" ? token.expiresIn : 3600, // Default to 1 hour
          createdAt: token.createdAt ?? new Date().toISOString(),
        };

        // Validate date format
        try {
          new Date(validatedToken.createdAt);
        } catch {
          validatedToken.createdAt = new Date().toISOString();
        }

        return validatedToken;
      });

      localStorage.setItem("oauth2Tokens", JSON.stringify(migratedTokens));
      return migratedTokens;
    } catch (error) {
      console.error("Error migrating tokens:", error);
      localStorage.setItem("oauth2Tokens", "[]");
      return [];
    }
  }

  const handleLogin = async () => {
    try {
      const response = await fetch("http://localhost:8080/auth/url");
      const data = (await response.json()) as UrlDto;
      window.location.href = data.url;
    } catch (error) {
      console.error("Error generating auth URL:", error);
    }
  };

  const handleRefreshToken = async (id: number, refreshToken: string) => {
    setRefreshStatus((prev) => ({ ...prev, [id]: "loading" }));

    try {
      const response = await fetch(
        `http://localhost:8080/auth/refresh?refresh_token=${refreshToken}`,
        {
          method: "POST",
        }
      );

      if (!response.ok) {
        throw new Error("Failed to refresh token");
      }

      const newTokenData = (await response.json()) as TokenDto;

      // Make sure expiresInSeconds exists, default to 3600 if not
      const expiresIn = newTokenData.expiresInSeconds || 3600;

      // Update tokens in state and localStorage
      const updatedTokens = tokens.map((token) => {
        if (token.id === id) {
          return {
            ...token,
            accessToken: newTokenData.accessToken,
            expiresIn: expiresIn,
            createdAt: new Date().toISOString(),
          };
        }
        return token;
      });

      setTokens(updatedTokens);
      localStorage.setItem("oauth2Tokens", JSON.stringify(updatedTokens));
      setRefreshStatus((prev) => ({ ...prev, [id]: "success" }));

      // Clear success status after 3 seconds
      setTimeout(() => {
        setRefreshStatus((prev) => ({ ...prev, [id]: null }));
      }, 3000);
    } catch (error) {
      console.error("Error refreshing token:", error);
      setRefreshStatus((prev) => ({ ...prev, [id]: "error" }));

      // Clear error status after 3 seconds
      setTimeout(() => {
        setRefreshStatus((prev) => ({ ...prev, [id]: null }));
      }, 3000);
    }
  };

  const handleDeleteToken = (id: number) => {
    const updatedTokens = tokens.filter((token) => token.id !== id);
    setTokens(updatedTokens);
    localStorage.setItem("oauth2Tokens", JSON.stringify(updatedTokens));
  };

  // Callback handler for processing OAuth response
  const processCallback = async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get("code");

    if (!code) {
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/auth/callback?code=${code}`
      );

      if (!response.ok) {
        throw new Error("Failed to exchange authorization code for tokens");
      }

      const tokenData = (await response.json()) as TokenDto;

      // Save tokens to localStorage with proper validation
      const savedTokens = migrateExistingTokens();
      const newToken: SavedToken = {
        id: Date.now(),
        accessToken: tokenData.accessToken,
        refreshToken: tokenData.refreshToken,
        expiresIn: tokenData.expiresInSeconds || 3600, // Default to 1 hour if not provided
        createdAt: new Date().toISOString(),
      };
      savedTokens.push(newToken);
      localStorage.setItem("oauth2Tokens", JSON.stringify(savedTokens));

      // Update tokens state
      setTokens(savedTokens);

      // Remove the code from URL to prevent reprocessing
      window.history.replaceState({}, document.title, window.location.pathname);
    } catch (err) {
      console.error("Error processing authentication:", err);
    }
  };

  // Check for auth code on mount
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get("code");

    if (code) {
      processCallback();
    }
  }, []);

  const checkTokenValidity = (createdAt: string, expiresIn: number) => {
    try {
      const createdDate = new Date(createdAt);

      // Check if createdAt is a valid date
      if (isNaN(createdDate.getTime())) {
        return "Invalid";
      }

      // Ensure expiresIn is a valid number
      if (typeof expiresIn !== "number" || isNaN(expiresIn)) {
        return "Invalid";
      }

      const expiryTime = createdDate.getTime() + expiresIn * 1000;
      const now = new Date().getTime();

      if (expiryTime < now) {
        return "Invalid";
      }

      return "Valid";
    } catch (error) {
      console.error("Error checking token validity:", error);
      return "Invalid";
    }
  };

  return (
    <div className="min-h-screen bg-white p-6">
      <div className="max-w-2xl mx-auto p-6 border border-gray-200 rounded">
        {/* Login Section */}
        <div className="text-center mb-8">
          <button
            onClick={handleLogin}
            className="bg-black text-white font-bold py-2 px-6 rounded"
          >
            Login with Google
          </button>
        </div>

        {/* Token Display Section */}
        <div>
          <h2 className="text-xl font-semibold mb-4">Access Tokens</h2>

          {tokens.length === 0 ? (
            <p className="text-center py-4 text-gray-500">
              No tokens available. Please authenticate to generate tokens.
            </p>
          ) : (
            <div className="space-y-4">
              {tokens.map((token) => (
                <div
                  key={token.id}
                  className="border border-gray-200 rounded p-4"
                >
                  <div className="flex justify-between items-start mb-2">
                    <div className="font-medium">
                      Status:{" "}
                      {checkTokenValidity(token.createdAt, token.expiresIn)}
                    </div>
                    <div className="space-x-2">
                      <button
                        onClick={() =>
                          handleRefreshToken(token.id, token.refreshToken)
                        }
                        disabled={refreshStatus[token.id] === "loading"}
                        className="bg-black text-white text-sm py-1 px-3 rounded"
                      >
                        {refreshStatus[token.id] === "loading"
                          ? "..."
                          : "Refresh"}
                      </button>
                      <button
                        onClick={() => handleDeleteToken(token.id)}
                        className="border border-black text-sm py-1 px-3 rounded"
                      >
                        Remove
                      </button>
                    </div>
                  </div>

                  {refreshStatus[token.id] === "success" && (
                    <div className="border border-gray-200 p-2 rounded mb-2">
                      Token refreshed successfully!
                    </div>
                  )}

                  {refreshStatus[token.id] === "error" && (
                    <div className="border border-gray-200 p-2 rounded mb-2">
                      Failed to refresh token.
                    </div>
                  )}

                  <div>
                    <div className="text-sm font-medium text-gray-500">
                      Access Token
                    </div>
                    <div className="bg-gray-100 p-2 rounded overflow-x-auto">
                      <code className="text-sm break-all">
                        {token.accessToken}
                      </code>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;
