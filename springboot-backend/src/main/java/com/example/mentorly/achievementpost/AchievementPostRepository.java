package com.example.mentorly.achievementpost;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementPostRepository extends MongoRepository<AchievementPost, String> {
    // Find posts by creator user ID
    List<AchievementPost> findByAuthorId(String creatorUserId);

    // Find posts by creator user ID with sorting
    List<AchievementPost> findByAuthorId(String creatorUserId, Sort sort);

    // Find posts by creator user ID in a given list of IDs (e.g., for fetching posts from users you follow)
    List<AchievementPost> findByAuthorIdIn(List<String> creatorUserIds);

    // Find posts by creator user ID in a given list of IDs with sorting
    List<AchievementPost> findByAuthorIdIn(List<String> creatorUserIds, Sort sort);

    // Find posts that a specific user has liked

    // Find posts that a specific user has liked
    List<AchievementPost> findByLikedUserIdsContaining(String userId);
}
