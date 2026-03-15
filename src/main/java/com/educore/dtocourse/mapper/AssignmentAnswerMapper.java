package com.educore.dtocourse.mapper;

import com.educore.assignment.StudentAssignmentAttempt;
import com.educore.assignment.assignmentQuestion.AssignmentQuestion;
import com.educore.assignment.assignmentQuestion.StudentAssignmentAnswer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AssignmentAnswerMapper {

    public List<StudentAssignmentAnswer> toStudentAnswersWithQuestions(
            StudentAssignmentAttempt attempt,
            Map<Long, String> answersMap,
            Map<Long, AssignmentQuestion> questionMap) {

        if (answersMap == null || attempt == null || questionMap == null) return null;

        return answersMap.entrySet().stream()
                .map(entry -> {
                    AssignmentQuestion question = questionMap.get(entry.getKey());
                    if (question == null) return null;

                    return StudentAssignmentAnswer.builder()
                            .attempt(attempt)
                            .question(question)
                            .selectedAnswer(entry.getValue())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}