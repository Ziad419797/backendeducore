package com.educore.assignment;

import com.educore.dtocourse.request.SubmitAssignmentRequest;
import com.educore.dtocourse.request.AssignmentAnswerRequest;
import com.educore.exception.AssignmentValidationException; // تأكدي من إنشاء هذا الاستثناء
import com.educore.assignment.Assignment;
import com.educore.assignment.assignmentQuestion.AssignmentQuestion;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class AssignmentValidator {

    /**
     * التحقق من صحة طلب تسليم الواجب
     */
    public void validateSubmitRequest(SubmitAssignmentRequest request, Long assignmentId) {
        if (request == null) {
            throw new AssignmentValidationException("طلب التسليم فارغ");
        }



        if (request.getAssignmentId() == null) {
            throw new AssignmentValidationException("معرف الواجب مطلوب");
        }

        if (!assignmentId.equals(request.getAssignmentId())) {
            throw new AssignmentValidationException("معرف الواجب في المسار لا يتطابق مع المعرف في الطلب");
        }

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new AssignmentValidationException("إجابات الواجب مطلوبة");
        }

        // التحقق من عدم تكرار الإجابات
        Set<Long> questionIds = request.getAnswers().stream()
                .map(AssignmentAnswerRequest::getQuestionId)
                .collect(Collectors.toSet());

        if (questionIds.size() != request.getAnswers().size()) {
            throw new AssignmentValidationException("يوجد إجابات مكررة لنفس السؤال في الواجب");
        }
    }

    /**
     * التحقق من أن جميع الأسئلة تمت الإجابة عليها
     */
    public void validateAllQuestionsAnswered(Assignment assignment, Map<Long, String> answers) {
        Set<Long> answeredIds = answers.keySet();
        Set<Long> assignmentQuestionIds = assignment.getQuestions().stream()
                .map(AssignmentQuestion::getId)
                .collect(Collectors.toSet());

        if (!answeredIds.containsAll(assignmentQuestionIds)) {
            Set<Long> missing = new HashSet<>(assignmentQuestionIds);
            missing.removeAll(answeredIds);
            throw new AssignmentValidationException("لم يتم الإجابة على جميع أسئلة الواجب. المفقود: " + missing);
        }
    }

    /**
     * التحقق مما إذا كان الواجب قد تجاوز الموعد النهائي للتسليم
     */
    public boolean isAssignmentExpired(Assignment assignment) {
        return assignment.getDeadline() != null &&
                LocalDateTime.now().isAfter(assignment.getDeadline());
    }
}