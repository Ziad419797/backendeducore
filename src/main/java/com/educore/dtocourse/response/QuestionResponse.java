package com.educore.dtocourse.response;

import com.educore.question.AnswerOption;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class QuestionResponse {
    private Long id;
    private String imageUrl;
    private Integer mark;
    private String description;
    private List<String> options; // تغيير من AnswerOption إلى String
    private Integer optionsCount; // عدد الخيارات
}
