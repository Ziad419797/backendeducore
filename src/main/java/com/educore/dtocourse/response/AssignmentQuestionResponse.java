package com.educore.dtocourse.response;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public class AssignmentQuestionResponse {
    private Long id;
    private String imageUrl;
    private Integer mark;
    private String description;
    private List<String> options; // الخيارات (A, B, C...)
    private Integer optionsCount; // عدد الخيارات المتاحة
}