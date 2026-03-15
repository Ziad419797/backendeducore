package com.educore.dtocourse.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateQuizRequest {
    @NotNull
    private Long weekId;

    @NotBlank
    private String title;
    @NotNull
    @Min(value = 5, message = "مدة الامتحان يجب أن تكون5 دقيقة  على الأقل")
    private Integer durationMinutes; // مدة الامتحان

    @NotEmpty
    private List<CreateQuestionRequest> questions;
}
