package com.educore.quiz;

import com.educore.common.PageResponse;
import com.educore.common.SortValidator;
import com.educore.dtocourse.mapper.QuizResultMapper;
import com.educore.dtocourse.response.QuizResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
@Tag(name = "Quiz Attempts", description = "APIs for managing quiz attempts")
public class QuizAttemptController {

    private final QuizAttemptService attemptService;
    private final QuizResultMapper resultMapper;

    @GetMapping("/{attemptId}")
    @Operation(summary = "Get attempt by ID", description = "Returns a single attempt by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    public ResponseEntity<QuizResultResponse> getAttemptById(
            @Parameter(description = "ID of the attempt")
            @PathVariable Long attemptId
    ) {
        StudentQuizAttempt attempt = attemptService.getAttemptById(attemptId);
        QuizResultResponse response = resultMapper.toDetailResponse(attempt, attempt.getQuiz());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/quiz/{quizId}")
    @Operation(summary = "Get attempt by student and quiz",
            description = "Returns attempt for specific student and quiz")
    public ResponseEntity<QuizResultResponse> getAttemptByStudentAndQuiz(
            @Parameter(description = "ID of the student") @PathVariable Long studentId,
            @Parameter(description = "ID of the quiz") @PathVariable Long quizId
    ) {
        StudentQuizAttempt attempt = attemptService.getAttemptByStudentAndQuiz(studentId, quizId);
        QuizResultResponse response = resultMapper.toDetailResponse(attempt, attempt.getQuiz());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all attempts by student",
            description = "Returns paginated list of attempts for a student")
    public ResponseEntity<Page<QuizResultResponse>> getAttemptsByStudent(
            @Parameter(description = "ID of the student") @PathVariable Long studentId,
            @PageableDefault(size = 10, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<StudentQuizAttempt> attemptsPage = attemptService.getAttemptsByStudent(studentId, pageable);

        Page<QuizResultResponse> responsePage = attemptsPage.map(attempt ->
                resultMapper.toDetailResponse(attempt, attempt.getQuiz())
        );

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get all attempts by quiz",
            description = "Returns paginated list of attempts for a quiz")
    public ResponseEntity<Page<QuizResultResponse>> getAttemptsByQuiz(
            @Parameter(description = "ID of the quiz") @PathVariable Long quizId,
            @PageableDefault(size = 10, sort = "score", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<StudentQuizAttempt> attemptsPage = attemptService.getAttemptsByQuiz(quizId, pageable);

        // نحتاج لجلب الكويز مرة واحدة فقط للتوفير
        if (attemptsPage.isEmpty()) {
            return ResponseEntity.ok(Page.empty());
        }

        // استخدام الكويز من أول محاولة (كل المحاولات لنفس الكويز)
        Quiz quiz = attemptsPage.getContent().get(0).getQuiz();

        Page<QuizResultResponse> responsePage = attemptsPage.map(attempt ->
                resultMapper.toDetailResponse(attempt, quiz)
        );

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/student/{studentId}/status")
    @Operation(summary = "Get attempts by student and status",
            description = "Returns paginated list of attempts filtered by submission status")
    public ResponseEntity<Page<QuizResultResponse>> getAttemptsByStudentAndStatus(
            @Parameter(description = "ID of the student") @PathVariable Long studentId,
            @Parameter(description = "Submission status (true=submitted, false=pending)")
            @RequestParam Boolean submitted,
            @PageableDefault(size = 10, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<StudentQuizAttempt> attemptsPage = attemptService.getAttemptsByStudentAndStatus(studentId, submitted, pageable);

        Page<QuizResultResponse> responsePage = attemptsPage.map(attempt ->
                resultMapper.toDetailResponse(attempt, attempt.getQuiz())
        );

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/quiz/{quizId}/statistics")
    @Operation(summary = "Get quiz statistics",
            description = "Returns statistics for a specific quiz")
    public ResponseEntity<QuizAttemptService.QuizStatistics> getQuizStatistics(
            @Parameter(description = "ID of the quiz") @PathVariable Long quizId
    ) {
        return ResponseEntity.ok(attemptService.getQuizStatistics(quizId));
    }

    @DeleteMapping("/{attemptId}")
    @Operation(summary = "Delete attempt", description = "Deletes an attempt (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    public ResponseEntity<Void> deleteAttempt(
            @Parameter(description = "ID of the attempt") @PathVariable Long attemptId
    ) {
        attemptService.deleteAttempt(attemptId);
        return ResponseEntity.noContent().build();
    }
}