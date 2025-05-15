import { fetchWithAuth } from "./apiClient";

const BASE = "/api/v1/plans";

export const planService = {
  getAll: () => fetchWithAuth(BASE),
  getById: (id) => fetchWithAuth(`${BASE}/${id}`),
  create: (data) =>
    fetchWithAuth(BASE, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  update: (id, data) =>
    fetchWithAuth(`${BASE}/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  delete: (id) =>
    fetchWithAuth(`${BASE}/${id}`, {
      method: "DELETE",
    }),
  addComment: (id, data) =>
    fetchWithAuth(`${BASE}/${id}/comments`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  editComment: (id, commentId, data) =>
    fetchWithAuth(`${BASE}/${id}/comments/${commentId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  removeComment: (id, commentId) =>
    fetchWithAuth(`${BASE}/${id}/comments/${commentId}`, {
      method: "DELETE",
    }),
  addLike: (id) => fetchWithAuth(`${BASE}/${id}/like`, { method: "POST" }),
  removeLike: (id) => fetchWithAuth(`${BASE}/${id}/like`, { method: "DELETE" }),

  getByAuthorId: (authorId) =>
    fetchWithAuth(`${BASE}/author/${authorId}`),  
};
