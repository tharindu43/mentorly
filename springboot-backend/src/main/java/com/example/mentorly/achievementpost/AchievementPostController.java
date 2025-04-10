package com.example.mentorly.achievementpost;

import com.example.mentorly.comment.Comment;
import com.example.mentorly.comment.CommentMapper;
import com.example.mentorly.comment.CommentRequest;
import com.example.mentorly.notification.NotificationDto;
import com.example.mentorly.notification.NotificationMapper;
import com.example.mentorly.notification.NotificationService;
import com.example.mentorly.notification.NotificationType;
import com.example.mentorly.user.User;
import com.example.mentorly.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
  
@RestController
@RequestMapping("/api/v1/achievement-posts")
@RequiredArgsConstructor
public class AchievementPostController {

    private final AchievementPostService achievementPostService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final AchievementPostMapper achievementPostMapper;
    private final CommentMapper commentMapper;
    private final NotificationMapper notificationMapper;

    @PostMapping
    public ResponseEntity<AchievementPostDto> saveAchievementPost(
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal,
            @Valid @RequestBody AchievementPostRequest request
    ) {
        // Convert request to entity
        AchievementPost achievementPost = achievementPostMapper.toEntity(request);

        // Set author details
        String googleId = principal.getAttributes().get("sub").toString();
        var user = userService.findByGoogleId(googleId);

        achievementPost.setAuthorId(user.getId().toString());
        achievementPost.setProfileImageUrl(user.getProfileImageUrl());
        achievementPost.setAuthorName(principal.getName());

        // Save and convert to DTO
        AchievementPost savedPost = achievementPostService.save(achievementPost);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(achievementPostMapper.toDto(savedPost));
    }

    @GetMapping
    public ResponseEntity<List<AchievementPostDto>> getAllAchievementPosts() {
        List<AchievementPost> allAchievementPosts = achievementPostService.findAll();
        List<AchievementPostDto> dtos = allAchievementPosts.stream()
                .map(achievementPostMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{achievement-post-id}")
    public ResponseEntity<AchievementPostDto> getAchievementPostById(
            @PathVariable("achievement-post-id") String achievementPostId) {
        AchievementPost achievementPost = achievementPostService.findById(achievementPostId);
        return ResponseEntity.ok(achievementPostMapper.toDto(achievementPost));
    }

    @PutMapping("/{achievement-post-id}")
    public ResponseEntity<AchievementPostDto> updateAchievementPost(
            @PathVariable("achievement-post-id") String achievementPostId,
            @Valid @RequestBody AchievementPostRequest request,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        // Get existing post
        AchievementPost existingPost = achievementPostService.findById(achievementPostId);

        // Check if user is the author
        String googleId = principal.getAttributes().get("sub").toString();
        String userId = userService.findByGoogleId(googleId)
                .getId()
                .toString();

        if (!existingPost.getAuthorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update fields while preserving metadata
        achievementPostMapper.updateEntityFromRequest(request, existingPost);

        // Save and return
        AchievementPost updatedPost = achievementPostService.update(existingPost);
        return ResponseEntity.ok(achievementPostMapper.toDto(updatedPost));
    }

    @DeleteMapping("/{achievement-post-id}")
    public ResponseEntity<Void> deleteAchievementPost(
            @PathVariable("achievement-post-id") String achievementPostId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        // Get existing post
        AchievementPost existingPost = achievementPostService.findById(achievementPostId);

        // Check if user is the author
        String googleId = principal.getAttributes().get("sub").toString();
        String userId = userService.findByGoogleId(googleId)
                .getId()
                .toString();

        if (!existingPost.getAuthorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        achievementPostService.delete(achievementPostId);
        return ResponseEntity.noContent().build();
    }

    // Social interaction endpoints

    @PostMapping("/{achievement-post-id}/like")
    public ResponseEntity<AchievementPostDto> likeAchievementPost(
            @PathVariable("achievement-post-id") String achievementPostId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toString();

        AchievementPost updatedPost = achievementPostService.addLike(achievementPostId, userId, user.getName());

        return ResponseEntity.ok(achievementPostMapper.toDto(updatedPost));
    }

    @DeleteMapping("/{achievement-post-id}/like")
    public ResponseEntity<AchievementPostDto> unlikeAchievementPost(
            @PathVariable("achievement-post-id") String achievementPostId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        String userId = userService.findByGoogleId(googleId)
                .getId()
                .toString();

        AchievementPost updatedPost = achievementPostService.removeLike(achievementPostId, userId);
        return ResponseEntity.ok(achievementPostMapper.toDto(updatedPost));
    }

    @PostMapping("/{achievement-post-id}/comments")
    public ResponseEntity<AchievementPostDto> addComment(
            @PathVariable("achievement-post-id") String achievementPostId,
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        AchievementPost post = achievementPostService.findById(achievementPostId);

        // Set comment metadata
        String googleId = principal.getAttributes().get("sub").toString();
        var user = userService.findByGoogleId(googleId);

        Comment comment = commentMapper.toEntity(commentRequest);

        comment.setCommentId(new ObjectId());
        comment.setAuthorId(user.getId().toString());
        comment.setAuthorName(principal.getName());
        comment.setProfileImageUrl(user.getProfileImageUrl());

        // Add comment to post
        post.getComments().add(comment);
        AchievementPost updatedPost = achievementPostService.update(post);
        //Add Notification
        String notificationText = " commented on your " + "'" + post.getTitle() + "'" + " achievement post.";
        NotificationDto notificationDto = new NotificationDto(
                null,
                post.getAuthorId(),
                user.getName(),
                post.getAchievementPostId().toHexString(),
                NotificationType.COMMENT,
                notificationText,
                LocalDateTime.now(),
                false
        );

        notificationService.create(notificationMapper.toEntity(notificationDto));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(achievementPostMapper.toDto(updatedPost));
    }

    @DeleteMapping("/{achievement-post-id}/comments/{comment-id}")
    public ResponseEntity<AchievementPostDto> deleteComment(
            @PathVariable("achievement-post-id") String achievementPostId,
            @PathVariable("comment-id") String commentId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        AchievementPost post = achievementPostService.findById(achievementPostId);

        // Get user ID
        String googleId = principal.getAttributes().get("sub").toString();
        String userId = userService.findByGoogleId(googleId)
                .getId()
                .toString();
        ObjectId commentObjectId = new ObjectId(commentId);

        // Find comment
        Comment commentToDelete = post.getComments().stream()
                .filter(c -> c.getCommentId().equals(commentObjectId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if user is either comment author or post owner
        if (!commentToDelete.getAuthorId().equals(userId) && !post.getAuthorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Remove comment
        post.getComments().removeIf(c -> c.getCommentId().equals(commentObjectId));
        AchievementPost updatedPost = achievementPostService.update(post);

        return ResponseEntity.ok(achievementPostMapper.toDto(updatedPost));
    }

    @PutMapping("/{achievement-post-id}/comments/{comment-id}")
    public ResponseEntity<AchievementPostDto> updateComment(
            @PathVariable("achievement-post-id") String achievementPostId,
            @PathVariable("comment-id") String commentId,
            @Valid @RequestBody CommentRequest updatedCommentRequest,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        AchievementPost post = achievementPostService.findById(achievementPostId);

        // Get user ID
        String googleId = principal.getAttributes().get("sub").toString();
        String userId = userService.findByGoogleId(googleId)
                .getId()
                .toString();

        ObjectId commentObjectId = new ObjectId(commentId);

        // Find comment
        Comment existingComment = post.getComments().stream()
                .filter(c -> c.getCommentId().equals(commentObjectId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if user is comment author
        if (!existingComment.getAuthorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Comment updatedComment = commentMapper.toEntity(updatedCommentRequest);

        // Update only the content, preserve metadata
        existingComment.setContent(updatedComment.getContent());

        AchievementPost updatedPost = achievementPostService.update(post);

        return ResponseEntity.ok(achievementPostMapper.toDto(updatedPost));
    }

    @GetMapping("/user/{user-id}")
    public ResponseEntity<List<AchievementPostDto>> getPostsByUser(
            @PathVariable("user-id") String userId) {
        List<AchievementPost> posts = achievementPostService.findByUserId(userId);
        List<AchievementPostDto> postDtos = posts.stream()
                .map(achievementPostMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(postDtos);
    }

    @GetMapping("/me")
    public ResponseEntity<List<AchievementPostDto>> getCurrentUserPosts(
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal) {

        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);

        List<AchievementPost> posts = achievementPostService.findByUserId(user.getId().toHexString());
        List<AchievementPostDto> postDtos = posts.stream()
                .map(achievementPostMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(postDtos);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<AchievementPostDto>> getFeed(
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal) {

        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);

        // Get IDs of users that the current user follows
        List<String> followingIds = user.getFollowing().stream()
                .map(ObjectId::toHexString)
                .collect(Collectors.toList());

        List<AchievementPost> posts = achievementPostService.getFeedForUser(
                user.getId().toHexString(), followingIds);

        List<AchievementPostDto> postDtos = posts.stream()
                .map(achievementPostMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(postDtos);
    }

    @GetMapping("/liked")
    public ResponseEntity<List<AchievementPostDto>> getLikedPosts(
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal) {

        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);

        List<AchievementPost> posts = achievementPostService.findLikedByUser(user.getId().toHexString());
        List<AchievementPostDto> postDtos = posts.stream()
                .map(achievementPostMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(postDtos);
    }
}