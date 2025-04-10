package com.example.mentorly.achievementpost;

import com.example.mentorly.comment.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class AchievementPostMapper {
    private final CommentMapper commentMapper;

    public AchievementPost toEntity(AchievementPostRequest request) {
        return AchievementPost.builder()
                .skill(request.getSkill())
                .title(request.getTitle())
                .templateType(request.getTemplateType())
                .templateData(request.getTemplateData())
                .postedDate(LocalDateTime.now())
                .noOfLikes(0)
                .likedUserIds(new HashSet<>())
                .comments(new ArrayList<>())
                .build();
    }

    public AchievementPostDto toDto(AchievementPost entity) {
        return new AchievementPostDto(
                entity.getAchievementPostId() != null ? entity.getAchievementPostId().toHexString() : null,
                entity.getAuthorId(),
                entity.getAuthorName(),
                entity.getPostedDate(),
                entity.getProfileImageUrl(),
                entity.getSkill(),
                entity.getTitle(),
                entity.getTemplateType(),
                entity.getTemplateData(),
                entity.getLikedUserIds(),
                entity.getNoOfLikes(),
                entity.getComments().stream().map(commentMapper::toDto).toList()
        );
    }

    public void updateEntityFromRequest(AchievementPostRequest request, AchievementPost entity) {
        entity.setSkill(request.getSkill());
        entity.setTitle(request.getTitle());
        entity.setTemplateType(request.getTemplateType());
        entity.setTemplateData(request.getTemplateData());
    }
}