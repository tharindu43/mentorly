package com.example.mentorly.plans;

import com.example.mentorly.comment.Comment;
import com.example.mentorly.enrollment.EnrollmentRepository;
import com.example.mentorly.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final EnrollmentRepository enrollmentRepository;

    // plan operations
    public Plan createPlan(Plan plan) {
        return planRepository.save(plan);
    }

    public Plan getPlanById(String id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll(Sort.by(Sort.Direction.DESC, "publishedDate"));
    }

    public Plan updatePlan(Plan plan) {
        // Check if exists
        getPlanById(plan.getId().toHexString());
        return planRepository.save(plan);
    }

    public void deletePlan(String id) {
        // Check if exists
        getPlanById(id);
        planRepository.deleteById(id);
    }

    public List<Plan> getPlansByAuthor(String authorId) {
        return planRepository.findByAuthorId(authorId,
                Sort.by(Sort.Direction.DESC, "publishedDate"));
    }

    public List<Plan> getPlansByCategory(String category) {
        return planRepository.findByCategory(category);
    }

    public List<Plan> getPlansByTag(String tag) {
        return planRepository.findByTagsContaining(tag);
    }

    public List<Plan> getLikedPlans(String userId) {
        return planRepository.findByLikedUserIdsContaining(userId);
    }

    // View count operations
    public Plan incrementViewCount(String planId) {
        Plan plan = getPlanById(planId);
        plan.setTotalView(plan.getTotalView() + 1);
        return planRepository.save(plan);
    }

    // Like operations
    public Plan addLike(String planId, String userId) {
        Plan plan = getPlanById(planId);

        if (plan.getLikedUserIds().add(userId)) {
            plan.setNoOfLikes(plan.getNoOfLikes() + 1);
        }

        return planRepository.save(plan);
    }

    public Plan removeLike(String planId, String userId) {
        Plan plan = getPlanById(planId);

        if (plan.getLikedUserIds().remove(userId)) {
            plan.setNoOfLikes(Math.max(0, plan.getNoOfLikes() - 1));
        }

        return planRepository.save(plan);
    }

    // Comment operations
    public Plan addComment(String planId, Comment comment) {
        Plan plan = getPlanById(planId);

        if (plan.getComments() == null) {
            plan.setComments(new ArrayList<>());
        }

        plan.getComments().add(comment);
        return planRepository.save(plan);
    }

    public Plan updateComment(String planId, String commentId, String content) {
        Plan plan = getPlanById(planId);

        plan.getComments().stream()
                .filter(c -> c.getCommentId().equals(new ObjectId(commentId)))
                .findFirst()
                .ifPresent(comment -> comment.setContent(content));

        return planRepository.save(plan);
    }

    public Plan deleteComment(String planId, String commentId) {
        Plan plan = getPlanById(planId);

        plan.getComments().removeIf(comment ->
                comment.getCommentId().equals(new ObjectId(commentId)));

        return planRepository.save(plan);
    }

    public boolean isUserEnrolled(String userId, String planId) {
        return enrollmentRepository.existsByUserIdAndPlanId(userId, new ObjectId(planId));
    }
}