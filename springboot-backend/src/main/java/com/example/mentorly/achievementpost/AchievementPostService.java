package com.example.mentorly.achievementpost;

import com.example.mentorly.exception.ResourceNotFoundException;
import com.example.mentorly.notification.NotificationDto;
import com.example.mentorly.notification.NotificationMapper;
import com.example.mentorly.notification.NotificationService;
import com.example.mentorly.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementPostService {

    private final AchievementPostRepository achievementPostRepository;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public AchievementPost save(AchievementPost achievementPost) {
        return this.achievementPostRepository.save(achievementPost);
    }

    public AchievementPost update(AchievementPost achievementPost) {
        // Check if exists first
        findById(achievementPost.getAchievementPostId().toHexString());
        return this.achievementPostRepository.save(achievementPost);
    }

    public AchievementPost findById(String id) {
        return this.achievementPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement post not found with id: " + id));
    }

    public List<AchievementPost> findAll() {
        return achievementPostRepository.findAll();
    }

    // Get all posts sorted by creation date (newest first)
    public List<AchievementPost> findAllSorted() {
        return achievementPostRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // Find posts by user ID
    public List<AchievementPost> findByUserId(String userId) {
        return achievementPostRepository.findByAuthorId(userId,
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // Find posts by users in the given list (e.g., users that the current user follows)
    public List<AchievementPost> findByUserIds(List<String> userIds) {
        return achievementPostRepository.findByAuthorIdIn(userIds,
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // Find posts liked by a specific user
    public List<AchievementPost> findLikedByUser(String userId) {
        return achievementPostRepository.findByLikedUserIdsContaining(userId);
    }

    // Find posts for feed (posts from followed users + own posts)
    public List<AchievementPost> getFeedForUser(String userId, List<String> followingUserIds) {
        if (!followingUserIds.contains(userId)) {
            followingUserIds.add(userId);
        }

        return achievementPostRepository.findByAuthorIdIn(followingUserIds,
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public void delete(String id) {
        // Check if exists first
        findById(id);
        this.achievementPostRepository.deleteById(id);
    }

    // Add like to post
    public AchievementPost addLike(String postId, String userId, String userName) {
        AchievementPost post = findById(postId);

        if (post.getLikedUserIds().add(userId)) {
            post.setNoOfLikes(post.getNoOfLikes() + 1);
            String notificationText = " liked your " + "'" + post.getTitle() + "'" + " achievement post.";

            NotificationDto notificationDto = new NotificationDto(
                    null,
                    post.getAuthorId(),
                    userName,
                    post.getAchievementPostId().toHexString(),
                    NotificationType.LIKE,
                    notificationText,
                    LocalDateTime.now(),
                    false
            );

            notificationService.create(notificationMapper.toEntity(notificationDto));
        }

        return achievementPostRepository.save(post);
    } 

    // Remove like from post
    public AchievementPost removeLike(String postId, String userId) {
        AchievementPost post = findById(postId);
        if (post.getNoOfLikes() > 0) {
            if (post.getLikedUserIds().remove(userId)) {
                post.setNoOfLikes(post.getNoOfLikes() - 1);
            }
        }
        return achievementPostRepository.save(post);
    }
}
