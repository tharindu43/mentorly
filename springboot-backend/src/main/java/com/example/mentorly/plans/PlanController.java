package com.example.mentorly.plans;

import com.example.mentorly.comment.Comment;
import com.example.mentorly.comment.CommentMapper;
import com.example.mentorly.comment.CommentRequest;
import com.example.mentorly.enrollment.EnrollmentService;
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
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EnrollmentService enrollmentService;
    private final PlanMapper planMapper;
    private final CommentMapper commentMapper;
    private final NotificationMapper notificationMapper;

    // Create a new plan
    @PostMapping
    public ResponseEntity<PlanDto> createPlan(
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal,
            @Valid @RequestBody PlanRequest request
    ) {
        // Get user details
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);

        // Create plan entity
        Plan plan = planMapper.toEntity(request);
        plan.setAuthorId(user.getId().toHexString());
        plan.setAuthorName(principal.getName());
        plan.setAuthorImg(user.getProfileImageUrl());

        // Save plan
        Plan savedPlan = planService.createPlan(plan);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(planMapper.toDto(savedPlan, user.getId().toHexString()));
    }

    // Get all plan
    @GetMapping
    public ResponseEntity<List<PlanDto>> getAllPlans(
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        List<Plan> plans = planService.getAllPlans();
        List<PlanDto> planDtos = plans.stream()
                .map(plan -> {
                    PlanDto dto = planMapper.toDto(plan, userId);
                    dto.setUserEnrolled(planService.isUserEnrolled(userId, plan.getId().toHexString()));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(planDtos);
    }

    // Get plan by ID
    @GetMapping("/{plan-id}")
    public ResponseEntity<PlanDto> getPlanById(
            @PathVariable("plan-id") String planId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        Plan plan = planService.getPlanById(planId);

        // Increment view count
        planService.incrementViewCount(planId);

        PlanDto dto = planMapper.toDto(plan, userId);
        dto.setUserEnrolled(planService.isUserEnrolled(userId, planId));

        return ResponseEntity.ok(dto);
    }

    // Update plan
    @PutMapping("/{plan-id}")
    public ResponseEntity<PlanDto> updatePlan(
            @PathVariable("plan-id") String planId,
            @Valid @RequestBody PlanRequest request,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        // Get user details
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        // Check if the user is the author
        Plan existingPlan = planService.getPlanById(planId);
        if (!existingPlan.getAuthorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update plan
        planMapper.updateEntityFromRequest(request, existingPlan);
        Plan updatedPlan = planService.updatePlan(existingPlan);

        return ResponseEntity.ok(planMapper.toDto(updatedPlan, userId));
    }

    // Delete plan
    @DeleteMapping("/{plan-id}")
    public ResponseEntity<Void> deletePlan(
            @PathVariable("plan-id") String planId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        // Get user details
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        // Check if the user is the author
        Plan existingPlan = planService.getPlanById(planId);
        if (!existingPlan.getAuthorId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Delete plan enrollments before deleting plan
        enrollmentService.deleteEnrollmentsByPlanId(planId);

        // Delete plan
        planService.deletePlan(planId);

        return ResponseEntity.noContent().build();
    }

    // Like plan
    @PostMapping("/{plan-id}/like")
    public ResponseEntity<PlanDto> likePlan(
            @PathVariable("plan-id") String planId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        Plan plan = planService.addLike(planId, userId);

        //Add Notification
        String notificationText = " liked your " + "'" + plan.getTitle() + "'" + " plan.";
        NotificationDto notificationDto = new NotificationDto(
                null,
                plan.getAuthorId(),
                user.getName(),
                plan.getId().toHexString(),
                NotificationType.LIKE,
                notificationText,
                LocalDateTime.now(),
                false
        );

        notificationService.create(notificationMapper.toEntity(notificationDto));

        return ResponseEntity.ok(planMapper.toDto(plan, userId));
    }

    // Unlike plan
    @DeleteMapping("/{plan-id}/like")
    public ResponseEntity<PlanDto> unlikePlan(
            @PathVariable("plan-id") String planId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        Plan plan = planService.removeLike(planId, userId);

        return ResponseEntity.ok(planMapper.toDto(plan, userId));
    }

    // Add comment to plan
    @PostMapping("/{plan-id}/comments")
    public ResponseEntity<PlanDto> addComment(
            @PathVariable("plan-id") String planId,
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        Comment comment = commentMapper.toEntity(commentRequest);
        comment.setCommentId(new ObjectId());
        comment.setAuthorId(userId);
        comment.setAuthorName(principal.getName());
        comment.setProfileImageUrl(user.getProfileImageUrl());
        Plan plan = planService.addComment(planId, comment);

        //Add Notification
        String notificationText = " commented on your " + "'" + plan.getTitle() + "'" + " plan.";
        NotificationDto notificationDto = new NotificationDto(
                null,
                plan.getAuthorId(),
                user.getName(),
                plan.getId().toHexString(),
                NotificationType.COMMENT,
                notificationText,
                LocalDateTime.now(),
                false
        );

        notificationService.create(notificationMapper.toEntity(notificationDto));

        return ResponseEntity.ok(planMapper.toDto(plan, userId));
    }

    // Update comment
    @PutMapping("/{plan-id}/comments/{comment-id}")
    public ResponseEntity<PlanDto> updateComment(
            @PathVariable("plan-id") String planId,
            @PathVariable("comment-id") String commentId,
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        // Verify that the user is the author of the comment (you would need to implement this check)
        Plan plan = planService.getPlanById(planId);
        boolean isCommentAuthor = plan.getComments().stream()
                .anyMatch(c -> c.getCommentId().equals(new ObjectId(commentId)) && c.getAuthorId().equals(userId));

        if (!isCommentAuthor) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Plan updatedPlan = planService.updateComment(planId, commentId, commentRequest.getContent());

        return ResponseEntity.ok(planMapper.toDto(updatedPlan, userId));
    }

    // Delete comment
    @DeleteMapping("/{plan-id}/comments/{comment-id}")
    public ResponseEntity<PlanDto> deleteComment(
            @PathVariable("plan-id") String planId,
            @PathVariable("comment-id") String commentId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        // Verify that the user is the author of the comment or the plan
        Plan plan = planService.getPlanById(planId);
        boolean isCommentAuthor = plan.getComments().stream()
                .anyMatch(c -> c.getCommentId().equals(new ObjectId(commentId)) && c.getAuthorId().equals(userId));
        boolean isPlanAuthor = plan.getAuthorId().equals(userId);

        if (!isCommentAuthor && !isPlanAuthor) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Plan updatedPlan = planService.deleteComment(planId, commentId);

        return ResponseEntity.ok(planMapper.toDto(updatedPlan, userId));
    }

    // Get plans by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<PlanDto>> getPlansByCategory(
            @PathVariable String category,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        List<Plan> plans = planService.getPlansByCategory(category);
        List<PlanDto> planDtos = plans.stream()
                .map(plan -> {
                    PlanDto dto = planMapper.toDto(plan, userId);
                    dto.setUserEnrolled(planService.isUserEnrolled(userId, plan.getId().toHexString()));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(planDtos);
    }

    // Get plans by tag
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<PlanDto>> getPlansByTag(
            @PathVariable String tag,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        List<Plan> plans = planService.getPlansByTag(tag);
        List<PlanDto> planDtos = plans.stream()
                .map(plan -> {
                    PlanDto dto = planMapper.toDto(plan, userId);
                    dto.setUserEnrolled(planService.isUserEnrolled(userId, plan.getId().toHexString()));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(planDtos);
    }

    // Get plans by author
    @GetMapping("/author/{author-id}")
    public ResponseEntity<List<PlanDto>> getPlansByAuthor(
            @PathVariable("author-id") String authorId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        List<Plan> plans = planService.getPlansByAuthor(authorId);
        List<PlanDto> planDtos = plans.stream()
                .map(plan -> {
                    PlanDto dto = planMapper.toDto(plan, userId);
                    dto.setUserEnrolled(planService.isUserEnrolled(userId, plan.getId().toHexString()));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(planDtos);
    }

    // Get liked plans
    @GetMapping("/liked")
    public ResponseEntity<List<PlanDto>> getLikedPlans(
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        List<Plan> plans = planService.getLikedPlans(userId);
        List<PlanDto> planDtos = plans.stream()
                .map(plan -> {
                    PlanDto dto = planMapper.toDto(plan, userId);
                    dto.setUserEnrolled(planService.isUserEnrolled(userId, plan.getId().toHexString()));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(planDtos);
    }
}