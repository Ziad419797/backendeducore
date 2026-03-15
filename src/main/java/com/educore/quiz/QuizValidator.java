package com.educore.quiz;

import com.educore.dtocourse.request.SubmitQuizRequest;
import com.educore.dtocourse.request.AnswerRequest;
import com.educore.exception.QuizValidationException;
import com.educore.question.AnswerOption;
import com.educore.question.Question;
import com.educore.quiz.Quiz;
import com.educore.quiz.StudentQuizAttempt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class QuizValidator {

    /**
     * التحقق من صحة طلب التسليم
     */
    public void validateSubmitRequest(SubmitQuizRequest request, Long quizId) {
        if (request == null) {
            throw new QuizValidationException("طلب التسليم فارغ");
        }



        if (request.getQuizId() == null) {
            throw new QuizValidationException("معرف الاختبار مطلوب");
        }

        // التحقق من تطابق quizId في المسار مع quizId في الطلب
        if (!quizId.equals(request.getQuizId())) {
            throw new QuizValidationException("معرف الاختبار في المسار لا يتطابق مع المعرف في الطلب");
        }

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new QuizValidationException("الإجابات مطلوبة");
        }

        // التحقق من عدم وجود إجابات مكررة
        Set<Long> questionIds = request.getAnswers().stream()
                .map(AnswerRequest::getQuestionId)
                .collect(Collectors.toSet());

        if (questionIds.size() != request.getAnswers().size()) {
            throw new QuizValidationException("يوجد إجابات مكررة لنفس السؤال");
        }
    }

    /**
     * التحقق من أن جميع الأسئلة تمت الإجابة عليها
     */
    public void validateAllQuestionsAnswered(
            Quiz quiz,
            Map<Long, String> answers) {

        Set<Long> answeredQuestionIds = answers.keySet();
        Set<Long> quizQuestionIds = quiz.getQuestions().stream()
                .map(Question::getId)
                .collect(Collectors.toSet());

        if (!answeredQuestionIds.containsAll(quizQuestionIds)) {
            Set<Long> missingQuestions = new HashSet<>(quizQuestionIds);
            missingQuestions.removeAll(answeredQuestionIds);

            throw new QuizValidationException(
                    "لم يتم الإجابة على جميع الأسئلة. الأسئلة المفقودة: " + missingQuestions);
        }

        if (answeredQuestionIds.size() > quizQuestionIds.size()) {
            throw new QuizValidationException(
                    "عدد الإجابات أكبر من عدد أسئلة الاختبار");
        }
    }

    /**
     * التحقق من انتهاء وقت الاختبار
     */
    public boolean isQuizExpired(StudentQuizAttempt attempt) {
        return attempt.getExpiresAt() != null &&
                LocalDateTime.now().isAfter(attempt.getExpiresAt());
    }
}