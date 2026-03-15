package com.educore.dtocourse.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubmitQuizRequest {
    private Long quizId;
    // 👇 إزالة studentId - مش محتاجينه لأنه من SecurityContext

    @NotEmpty(message = "Answers cannot be empty")
    private List<AnswerRequest> answers;

}
