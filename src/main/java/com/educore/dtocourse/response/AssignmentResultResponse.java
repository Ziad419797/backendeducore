package com.educore.dtocourse.response;

import lombok.*;

@Getter @Setter @AllArgsConstructor
@Builder
@NoArgsConstructor
public class AssignmentResultResponse {
    private Long attemptId;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private Integer score;
    private Integer totalMarks;
    private Double percentage;
    private Boolean passed;
    private Boolean submitted;
    private String submittedAt;
}
