package com.educore.dtocourse.request;

import com.educore.question.AnswerOption;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerRequest {
    private Long questionId;
    private String selectedAnswer; // تغيير من AnswerOption إلى String
}
