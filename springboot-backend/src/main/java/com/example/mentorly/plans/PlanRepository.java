package com.example.mentorly.plans;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends MongoRepository<Plan, String> {

    // Find plans by author ID
    List<Plan> findByAuthorId(String authorId);

    // Find plans by author ID with sorting
    List<Plan> findByAuthorId(String authorId, Sort sort);

    // Find plans by category
    List<Plan> findByCategory(String category);

    // Find plans by tag
    List<Plan> findByTagsContaining(String tag);

    // Find plans liked by a specific user
    List<Plan> findByLikedUserIdsContaining(String userId);
}
