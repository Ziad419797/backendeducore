package com.educore.dtocourse.mapper;

import com.educore.assignment.assignmentQuestion.StudentAssignmentAnswer;
import com.educore.dtocourse.request.SubmitAssignmentRequest;
import com.educore.dtocourse.request.AssignmentAnswerRequest;
import com.educore.assignment.Assignment;
import com.educore.assignment.assignmentQuestion.AssignmentQuestion;
import com.educore.assignment.StudentAssignmentAttempt;

import com.educore.student.Student;
import org.mapstruct.Mapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AssignmentSubmitMapper {

    // تحويل طلب التسليم إلى كائن محاولة (Attempt)
    default StudentAssignmentAttempt toAttemptWithAssignment(SubmitAssignmentRequest request, Assignment assignment, Student student) {
        if (request == null || assignment == null) return null;

        return StudentAssignmentAttempt.builder()
                .assignment(assignment)
                .student(student)
                .submitted(false)
                .build();
    }

    // تحويل قائمة الإجابات من الـ Request إلى Map لسهولة التصحيح
    default Map<Long, String> toAnswerMap(SubmitAssignmentRequest request) {
        if (request == null || request.getAnswers() == null) return null;

        return request.getAnswers().stream()
                .collect(Collectors.toMap(
                        AssignmentAnswerRequest::getQuestionId,
                        AssignmentAnswerRequest::getSelectedAnswer,
                        (existing, replacement) -> existing // في حالة وجود إجابتين لنفس السؤال نكتفي بالأولى
                ));
    }

    // تحويل الـ Map إلى قائمة كيانات StudentAssignmentAnswer لحفظها في الداتابيز
    default List<StudentAssignmentAnswer> toStudentAnswersFromMap(
            Map<Long, String> answersMap,
            StudentAssignmentAttempt attempt,
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
                .filter(answer -> answer != null)
                .collect(Collectors.toList());
    }

    // ميثود مساعدة للتحقق من أن الطالب أجاب على جميع الأسئلة
    default boolean validateAnswersCount(SubmitAssignmentRequest request, int expectedCount) {
        if (request == null || request.getAnswers() == null) return false;
        return request.getAnswers().size() == expectedCount;
    }
}