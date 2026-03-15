package com.educore.dtocourse.mapper;

import com.educore.assignment.Assignment;
import com.educore.assignment.assignmentQuestion.AssignmentQuestion;
import com.educore.dtocourse.response.AssignmentQuestionResponse;
import com.educore.dtocourse.response.AssignmentResponse;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    default AssignmentResponse toResponse(Assignment assignment) {
        if (assignment == null) return null;

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .active(assignment.getActive())
                .deadline(assignment.getDeadline())
                .weekId(assignment.getWeek() != null ? assignment.getWeek().getId() : null)
                .courseId(getCourseIdFromAssignment(assignment))
                .questions(mapQuestions(assignment.getQuestions()))
                .totalMarks(calculateTotalMarks(assignment.getQuestions()))
                .questionsCount(assignment.getQuestions() != null ? assignment.getQuestions().size() : 0)
                .createdAt(assignment.getCreatedAt())
                .build();
    }

    // ميثود لتحويل لستة الأسئلة بالكامل
    default List<AssignmentQuestionResponse> mapQuestions(Set<AssignmentQuestion> questions) {
        if (questions == null) return null;
        return questions.stream()
                .map(this::mapQuestion)
                .collect(Collectors.toList());
    }
    default AssignmentQuestionResponse mapQuestion(AssignmentQuestion question) {
        if (question == null) return null;

        return AssignmentQuestionResponse.builder()
                .id(question.getId())
                .description(question.getDescription())
                .imageUrl(question.getImageUrl())
                .mark(question.getMark())
                .options(question.getOptions())
                .optionsCount(question.getOptions() != null ? question.getOptions().size() : 0)
                .build();
    }

    // نفس المنطق السابق للوصول للكورس
    default Long getCourseIdFromAssignment(Assignment assignment) {
        try {
            if (assignment.getWeek() == null) return null;
            return assignment.getWeek().getSessions().iterator().next()
                    .getCourses().iterator().next().getId();
        } catch (Exception e) {
            return null;
        }
    }
    default Integer calculateTotalMarks(Set<AssignmentQuestion> questions) {
        if (questions == null) return 0;
        return questions.stream()
                .mapToInt(q -> q.getMark() != null ? q.getMark() : 0)
                .sum();
    }
}