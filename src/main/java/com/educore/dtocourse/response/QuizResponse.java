package com.educore.dtocourse.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
    private Long id;
    private String title;
//    private String description;
    private Boolean active;
    private Boolean deleted;
    private Boolean timeRestricted;
    private Integer durationMinutes;
    private Long weekId;
    private Long courseId;
    private List<QuestionResponse> questions;
    private Integer totalMarks;
    private Integer questionsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}