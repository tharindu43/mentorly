package com.example.mentorly.enrollment;

import com.example.mentorly.exception.ResourceNotFoundException;
import com.example.mentorly.plans.Plan;
import com.example.mentorly.plans.PlanService;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final PlanService planService;
    private final EnrollmentRepository enrollmentRepository;

    // Enrollment operations
    public Enrollment enrollInPlan(String planId, String userId) {
        ObjectId planObjectId = new ObjectId(planId);

        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndPlanId(userId, planObjectId)) {
            throw new RuntimeException("User is already enrolled in this plan");
        }

        // Get plan to create progress for each week
        Plan plan = planService.getPlanById(planId);

        // Create progress items
        List<Enrollment.WeekProgress> progress = plan.getWeeks().stream()
                .map(week -> Enrollment.WeekProgress.builder()
                        .week(week.getWeekNumber())
                        .completed(false)
                        .build())
                .collect(Collectors.toList());

        // Create enrollment
        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .planId(planObjectId)
                .enrolledAt(LocalDateTime.now())
                .progress(progress)
                .build();

        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getUserEnrollments(String userId) {
        return enrollmentRepository.findByUserId(userId);
    }

    public boolean isUserEnrolled(String userId, String planId) {
        return enrollmentRepository.existsByUserIdAndPlanId(userId, new ObjectId(planId));
    }

    public Enrollment updateProgress(String userId, String planId, WeekProgressRequest progressRequest) {
        ObjectId planObjectId = new ObjectId(planId);

        Enrollment enrollment = enrollmentRepository.findByUserIdAndPlanId(userId, planObjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for user: " + userId + " and plan: " + planId));

        // Find and update the week progress
        enrollment.getProgress().stream()
                .filter(p -> p.getWeek() == progressRequest.getWeek())
                .findFirst()
                .ifPresent(p -> p.setCompleted(progressRequest.isCompleted()));

        return enrollmentRepository.save(enrollment);
    }

    public Enrollment getEnrollment(String userId, String planId) {
        return enrollmentRepository.findByUserIdAndPlanId(userId, new ObjectId(planId))
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for user: " + userId + " and plan: " + planId));
    }

    public void deleteEnrollmentsByPlanId(String planId){
        ObjectId planObjectId = new ObjectId(planId);
        enrollmentRepository.deleteByPlanId(planObjectId);
    }
}
