package com.educore.dtocourse.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CreateAssignmentRequest {
    @NotNull
    private Long weekId;

    @NotBlank
    private String title;

    private String description; // وصف للواجب (اختياري)

    @NotNull
    private LocalDateTime deadline; // الموعد النهائي لتسليم الواجب

    @NotEmpty
    private List<CreateAssignmentQuestionRequest> questions;
}