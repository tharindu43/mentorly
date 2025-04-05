package com.example.mentorly.enrollment;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends MongoRepository<Enrollment, ObjectId> {

    // Find all enrollments for a specific user
    List<Enrollment> findByUserId(String userId);

    // Find all enrollments for a specific plan
    List<Enrollment> findByPlanId(ObjectId planId);

    // Find enrollment by user ID and plan ID
    Optional<Enrollment> findByUserIdAndPlanId(String userId, ObjectId planId);

    // Check if a user is enrolled in a plan
    boolean existsByUserIdAndPlanId(String userId, ObjectId planId);

    void deleteByPlanId(ObjectId planId);
}