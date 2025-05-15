package com.example.mentorly.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserRequest {
    @NotBlank String email;
}
// Compare this snippet from springboot-backend/src/main/java/com/example/mentorly/user/UserService.java:
