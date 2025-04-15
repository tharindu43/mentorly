package com.example.mentorly.achievementpost;

import com.example.mentorly.template.TemplateData;
import com.example.mentorly.template.TemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AchievementPostRequest {
    @NotBlank(message = "Skill cannot be blank")
    private String skill;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotNull(message = "Template type cannot be null")
    private TemplateType templateType;

    @NotNull(message = "Template data cannot be null")
    private TemplateData templateData;


    
}
