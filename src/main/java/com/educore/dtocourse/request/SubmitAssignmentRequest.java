package com.educore.dtocourse.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class SubmitAssignmentRequest {
    private Long assignmentId;

    @NotEmpty(message = "Answers cannot be empty")
    private List<AssignmentAnswerRequest> answers;
}
