package com.example.mentorly.plans;

import com.example.mentorly.comment.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlanMapper {

    private final CommentMapper commentMapper;

    public Plan toEntity(PlanRequest request) {
        return Plan.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .publishedDate(LocalDateTime.now())
                .totalView(0)
                .tags(request.getTags())
                .weeks(mapWeeks(request.getWeeks()))
                .likedUserIds(new HashSet<>())
                .noOfLikes(0)
                .build();
    }

    public PlanDto toDto(Plan plan, String currentUserId) {
        boolean userLiked = plan.getLikedUserIds().contains(currentUserId);

        return PlanDto.builder()
                .id(plan.getId().toHexString())
                .title(plan.getTitle())
                .description(plan.getDescription())
                .category(plan.getCategory())
                .authorId(plan.getAuthorId())
                .authorName(plan.getAuthorName())
                .authorImg(plan.getAuthorImg())
                .publishedDate(plan.getPublishedDate())
                .totalView(plan.getTotalView())
                .weeks(mapWeeksToDto(plan.getWeeks()))
                .tags(plan.getTags())
                .comments(plan.getComments().stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList()))
                .noOfLikes(plan.getNoOfLikes())
                .userLiked(userLiked)
                .build();
        
    }

    public void updateEntityFromRequest(PlanRequest request, Plan plan) {
        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.setCategory(request.getCategory());
        plan.setTags(request.getTags());
        plan.setWeeks(mapWeeks(request.getWeeks()));
    }

    private List<Plan.Week> mapWeeks(List<WeekRequest> weekRequests) {
        return weekRequests.stream()
                .map(this::mapWeek)
                .collect(Collectors.toList());
    }

    private Plan.Week mapWeek(WeekRequest weekRequest) {
        return Plan.Week.builder()
                .weekNumber(weekRequest.getWeekNumber())
                .title(weekRequest.getTitle())
                .content(weekRequest.getContent())
                .build();
    }

    private List<WeekDto> mapWeeksToDto(List<Plan.Week> weeks) {
        return weeks.stream()
                .map(this::mapWeekToDto)
                .collect(Collectors.toList());
    }

    private WeekDto mapWeekToDto(Plan.Week week) {
        return WeekDto.builder()
                .weekNumber(week.getWeekNumber())
                .title(week.getTitle())
                .content(week.getContent())
                .build();
    }
}
