package com.educore.dtocourse.mapper;

import com.educore.dtocourse.response.LeaderboardEntryResponse;
import com.educore.dtocourse.response.LeaderboardResponse;
import com.educore.quiz.StudentQuizAttempt;
import com.educore.quiz.Quiz;
import com.educore.course.Course;
import com.educore.student.Student;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring", imports = {DateTimeFormatter.class})
public interface LeaderboardMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== QUIZ LEADERBOARD ENTRY ====================

    @Mapping(target = "rank", source = "rank")
    @Mapping(target = "studentId", source = "attempt.student.id")
    @Mapping(target = "studentName", expression = "java(attempt.getStudent().getFullName())")
    @Mapping(target = "studentCode", source = "attempt.student.studentCode")
    @Mapping(target = "score", source = "attempt.score")
    @Mapping(target = "totalMarks", expression = "java(calculateTotalMarks(attempt.getQuiz()))")
    @Mapping(target = "percentage", expression = "java(calculatePercentage(attempt.getScore(), attempt.getQuiz()))")
    @Mapping(target = "quizId", source = "attempt.quiz.id")
    @Mapping(target = "quizTitle", source = "attempt.quiz.title")
    @Mapping(target = "completedAt", expression = "java(formatDate(attempt.getSubmittedAt()))")
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "courseName", ignore = true)
    LeaderboardEntryResponse toQuizEntry(StudentQuizAttempt attempt, Integer rank);

    default List<LeaderboardEntryResponse> toQuizEntryList(List<StudentQuizAttempt> attempts, int startRank) {
        if (attempts == null) return null;

        return java.util.stream.IntStream.range(0, attempts.size())
                .mapToObj(i -> toQuizEntry(attempts.get(i), startRank + i))
                .collect(java.util.stream.Collectors.toList());
    }
    // ==================== COURSE LEADERBOARD ENTRY ====================

    @Mapping(target = "rank", source = "rank")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(student != null ? student.getFullName() : null)")
    @Mapping(target = "studentCode", source = "student.studentCode")
    @Mapping(target = "score", source = "totalScore")
    @Mapping(target = "percentage", source = "averageScore")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseName", source = "course.title")
    @Mapping(target = "totalMarks", ignore = true)
    @Mapping(target = "quizId", ignore = true)
    @Mapping(target = "quizTitle", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    LeaderboardEntryResponse toCourseEntry(
            @MappingTarget LeaderboardEntryResponse entry,
            Student student,
            Course course,
            Integer totalScore,
            Double averageScore,
            Integer rank
    );

    default LeaderboardEntryResponse toCourseEntry(
            Student student,
            Course course,
            Integer totalScore,
            Double averageScore,
            Integer rank
    ) {
        return toCourseEntry(
                new LeaderboardEntryResponse(),
                student,
                course,
                totalScore,
                averageScore,
                rank
        );
    }

    // ==================== GLOBAL LEADERBOARD ENTRY ====================

    @Mapping(target = "rank", source = "rank")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(student != null ? student.getFullName() : null)")
    @Mapping(target = "studentCode", source = "student.studentCode")
    @Mapping(target = "score", source = "totalScore")
    @Mapping(target = "percentage", source = "averageScore")
    @Mapping(target = "totalMarks", source = "totalPossible")
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "courseName", ignore = true)
    @Mapping(target = "quizId", ignore = true)
    @Mapping(target = "quizTitle", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    LeaderboardEntryResponse toGlobalEntry(
            @MappingTarget LeaderboardEntryResponse entry,
            Student student,
            Integer totalScore,
            Double averageScore,
            Integer totalPossible,
            Integer rank
    );

    default LeaderboardEntryResponse toGlobalEntry(
            Student student,
            Integer totalScore,
            Double averageScore,
            Integer totalPossible,
            Integer rank
    ) {
        return toGlobalEntry(
                new LeaderboardEntryResponse(),
                student,
                totalScore,
                averageScore,
                totalPossible,
                rank
        );
    }

    // ==================== LEADERBOARD RESPONSE ====================

    @Mapping(target = "title", source = "title")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "entityId", source = "entityId")
    @Mapping(target = "entries", source = "entries")
    @Mapping(target = "totalParticipants", source = "totalParticipants")
    @Mapping(target = "averageScore", source = "averageScore")
    @Mapping(target = "highestScore", source = "highestScore")
    @Mapping(target = "lowestScore", source = "lowestScore")
    LeaderboardResponse toResponse(
            String title,
            String type,
            Long entityId,
            List<LeaderboardEntryResponse> entries,
            Integer totalParticipants,
            Double averageScore,
            Integer highestScore,
            Integer lowestScore
    );

    // ==================== HELPER METHODS ====================

    default Integer calculateTotalMarks(Quiz quiz) {
        if (quiz == null || quiz.getQuestions() == null) return 0;
        return quiz.getQuestions().stream()
                .mapToInt(q -> q.getMark() != null ? q.getMark() : 0)
                .sum();
    }

    default Double calculatePercentage(Integer score, Quiz quiz) {
        if (score == null || quiz == null) return 0.0;
        Integer totalMarks = calculateTotalMarks(quiz);
        if (totalMarks == 0) return 0.0;
        return (score.doubleValue() / totalMarks) * 100;
    }

    default String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATE_FORMATTER);
    }
}