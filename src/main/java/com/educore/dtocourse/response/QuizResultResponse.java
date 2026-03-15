package com.educore.dtocourse.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder // أضف Builder للسهولة
@NoArgsConstructor
public class QuizResultResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Long studentId;
    private Integer score;
    private Integer totalMarks;
    private Double percentage;
    private Boolean passed;
    private Boolean submitted;
    private String startedAt;
    private String submittedAt;
    private String expiresAt;
}
