package com.educore.dtocourse.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryResponse {
    private Integer rank;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Integer score;
    private Integer totalMarks;
    private Double percentage;
    private Long quizId;
    private String quizTitle;
    private Long courseId;
    private String courseName;
    private String completedAt;
}