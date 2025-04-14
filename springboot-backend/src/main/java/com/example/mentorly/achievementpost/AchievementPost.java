package com.example.mentorly.achievementpost;

import com.example.mentorly.comment.Comment;
import com.example.mentorly.template.TemplateData;
import com.example.mentorly.template.TemplateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
  
@Document("achievement-posts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AchievementPost {
    @Id private ObjectId achievementPostId;
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
    private List<Comment> comments;
    
}
