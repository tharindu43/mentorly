package com.example.mentorly.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private String id;
    private String email;
    private String name;
    private String profileImageUrl;
    private String bio;
    private int followersCount;
    private int followingCount;
    private boolean currentUserFollows;
}

// Compare this snippet from springboot-backend/src/main/java/com/example/mentorly/user/UserService.java:

