package com.example.mentorly.achievementpost;

import com.example.mentorly.comment.CommentDto;
import com.example.mentorly.template.TemplateData;
import com.example.mentorly.template.TemplateType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class AchievementPostDto {
    private String achievementPostId;
    private String authorId;
    private String authorName;
    private LocalDateTime postedDate;
    private String profileImageUrl;
    private String skill;
    private String title;
    private TemplateType templateType;
    private TemplateData templateData;
    private Set<String> likedUserIds;
    private int noOfLikes;
    private List<CommentDto> comments;
    
}
