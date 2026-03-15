package com.educore.dtocourse.mapper;

import com.educore.dtocourse.response.QuizResultResponse;
import com.educore.quiz.StudentQuizAttempt;
import com.educore.quiz.Quiz;
import com.educore.question.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuizResultMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // النتيجة المبسطة
    default QuizResultResponse toSimpleResponse(StudentQuizAttempt attempt, Quiz quiz) {
        if (attempt == null || quiz == null) return null;

        Integer totalMarks = calculateTotalMarks(quiz.getQuestions());

        return QuizResultResponse.builder()
                .score(attempt.getScore() != null ? attempt.getScore() : 0)
                .totalMarks(totalMarks)
                .build();
    }

    // النتيجة المفصلة
    default QuizResultResponse toDetailResponse(StudentQuizAttempt attempt, Quiz quiz) {
        if (attempt == null || quiz == null) return null;

        Integer totalMarks = calculateTotalMarks(quiz.getQuestions());
        Integer score = attempt.getScore() != null ? attempt.getScore() : 0;
        double percentage = calculatePercentage(score, totalMarks);

        return QuizResultResponse.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .studentId(attempt.getStudent().getId())
                .score(score)
                .totalMarks(totalMarks)
                .percentage(percentage)
                .passed(percentage >= 50.0)
                .submitted(attempt.getSubmitted())
                .startedAt(formatDate(attempt.getStartedAt()))
                .submittedAt(formatDate(attempt.getSubmittedAt()))
                .expiresAt(formatDate(attempt.getExpiresAt()))
                .build();
    }

    // نتيجة مع قائمة المحاولات
    default List<QuizResultResponse> toDetailResponseList(
            List<StudentQuizAttempt> attempts,
            Quiz quiz) {
        if (attempts == null || quiz == null) return null;

        return attempts.stream()
                .map(attempt -> toDetailResponse(attempt, quiz))
                .collect(Collectors.toList());
    }

    @Named("calculateTotalMarks")
    default Integer calculateTotalMarks(java.util.Collection<Question> questions) {
        if (questions == null) return 0;
        return questions.stream()
                .mapToInt(q -> q.getMark() != null ? q.getMark() : 0)
                .sum();
    }

    @Named("calculatePercentage")
    default double calculatePercentage(Integer score, Integer totalMarks) {
        if (score == null || totalMarks == null || totalMarks == 0) return 0.0;
        return (score.doubleValue() / totalMarks.doubleValue()) * 100;
    }

    @Named("formatDate")
    default String formatDate(java.time.LocalDateTime date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
}