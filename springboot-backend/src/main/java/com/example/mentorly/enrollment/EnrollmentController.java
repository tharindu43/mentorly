package com.example.mentorly.enrollment;

import com.example.mentorly.user.User;
import com.example.mentorly.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List; 

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final UserService userService;
    private final EnrollmentService enrollmentService;

    private final EnrollmentMapper enrollmentMapper;

    // Enroll in plan
    @PostMapping("/{plan-id}/enrol")
    public ResponseEntity<EnrollmentDto> enrollInPlan(
            @PathVariable("plan-id") String planId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        Enrollment enrollment = enrollmentService.enrollInPlan(planId, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentMapper.toDto(enrollment));
    }

    // Get user enrollments
    @GetMapping
    public ResponseEntity<List<EnrollmentDto>> getUserEnrollments(
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        List<Enrollment> enrollments = enrollmentService.getUserEnrollments(userId);
        List<EnrollmentDto> enrollmentDtos = enrollments.stream()
                .map(enrollmentMapper::toDto)
                .toList();

        return ResponseEntity.ok(enrollmentDtos);
    }

    // Get enrollment for a specific plan
    @GetMapping("/{plan-id}/enrolment")
    public ResponseEntity<EnrollmentDto> getEnrollment(
            @PathVariable("plan-id") String planId,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        Enrollment enrollment = enrollmentService.getEnrollment(userId, planId);

        return ResponseEntity.ok(enrollmentMapper.toDto(enrollment));
    }

    // Update progress
    @PutMapping("/{plan-id}/progress")
    public ResponseEntity<EnrollmentDto> updateProgress(
            @PathVariable("plan-id") String planId,
            @Valid @RequestBody WeekProgressRequest progressRequest,
            @AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal
    ) {
        String googleId = principal.getAttributes().get("sub").toString();
        User user = userService.findByGoogleId(googleId);
        String userId = user.getId().toHexString();

        Enrollment updatedEnrollment = enrollmentService.updateProgress(userId, planId, progressRequest);

        return ResponseEntity.ok(enrollmentMapper.toDto(updatedEnrollment));
    }
}
