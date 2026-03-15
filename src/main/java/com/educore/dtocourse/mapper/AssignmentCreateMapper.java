package com.educore.dtocourse.mapper;

import com.educore.dtocourse.request.CreateAssignmentRequest;
import com.educore.dtocourse.request.CreateAssignmentQuestionRequest;
import com.educore.assignment.Assignment;
import com.educore.assignment.assignmentQuestion.AssignmentQuestion;
import org.mapstruct.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AssignmentCreateMapper {

    default Assignment toEntity(CreateAssignmentRequest request) {
        if (request == null) return null;

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline()) // الواجب يعتمد على تاريخ انتهاء
                .active(true)
                .deleted(false)
                .build();

        if (request.getQuestions() != null) {
            assignment.setQuestions(
                    request.getQuestions().stream()
                            .map(q -> toQuestionEntity(q, assignment))
                            .collect(Collectors.toSet())
            );
        }

        return assignment;
    }

    default AssignmentQuestion toQuestionEntity(CreateAssignmentQuestionRequest request, Assignment assignment) {
        if (request == null) return null;

        return AssignmentQuestion.builder()
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .correctAnswer(request.getCorrectAnswer())
                .mark(request.getMark())
                .options(request.getOptions()) // تم إضافة الخيارات هنا
                .assignment(assignment)
                .build();
    }
}