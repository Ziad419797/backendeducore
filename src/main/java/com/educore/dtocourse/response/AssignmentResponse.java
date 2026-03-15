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
public class AssignmentResponse {
    private Long id;
    private String title;
    private String description; // وصف أو تعليمات الواجب
    private Boolean active;
    private Boolean deleted;
    private LocalDateTime deadline; // الموعد النهائي للتسليم
    private Long weekId;
    private Long courseId;
    private List<AssignmentQuestionResponse> questions; // قائمة الأسئلة
    private Integer totalMarks; // مجموع الدرجات
    private Integer questionsCount; // عدد الأسئلة
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}