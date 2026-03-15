package com.educore.dtocourse.mapper;

import com.educore.assignment.Assignment;
import com.educore.assignment.StudentAssignmentAttempt;
import com.educore.dtocourse.response.AssignmentResultResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssignmentResultMapper {

    default AssignmentResultResponse toDetailResponse(StudentAssignmentAttempt attempt, Assignment assignment) {
        if (attempt == null || assignment == null) return null;

        int totalMarks = assignment.getQuestions().stream().mapToInt(q -> q.getMark()).sum();
        int score = attempt.getScore() != null ? attempt.getScore() : 0;
        double percentage = totalMarks == 0 ? 0 : (double) score / totalMarks * 100;

        return AssignmentResultResponse.builder()
                .attemptId(attempt.getId())
                .assignmentId(assignment.getId())
                .assignmentTitle(assignment.getTitle())
                .studentId(attempt.getStudent().getId())
                .score(score)
                .totalMarks(totalMarks)
                .percentage(percentage)
                .passed(percentage >= 50.0)
                .submitted(attempt.getSubmitted())
                .submittedAt(attempt.getSubmittedAt() != null ? attempt.getSubmittedAt().toString() : null)
                .build();
    }
}
