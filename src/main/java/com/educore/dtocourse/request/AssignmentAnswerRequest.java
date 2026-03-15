package com.educore.dtocourse.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentAnswerRequest {

    @NotNull(message = "معرف السؤال مطلوب")
    private Long questionId;

    @NotBlank(message = "يجب اختيار إجابة")
    private String selectedAnswer; // الحرف أو النص الذي اختاره الطالب
}