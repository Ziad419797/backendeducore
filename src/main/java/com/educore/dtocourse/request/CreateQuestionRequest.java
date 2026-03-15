package com.educore.dtocourse.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateQuestionRequest {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Options are required")
    @Size(min = 2, max = 10, message = "Options must be between 2 and 10")
    private List<@NotBlank String> options; // قائمة الخيارات كنصوص

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer; // تغيير من AnswerOption إلى String

    @NotNull(message = "Mark is required")
    @Min(value = 1, message = "Mark must be at least 1")
    private Integer mark;
}