package com.educore.dtocourse.mapper;

import com.educore.dtocourse.request.SubmitQuizRequest;
import com.educore.dtocourse.request.AnswerRequest;
import com.educore.quiz.StudentQuizAttempt;
import com.educore.student.Student;
import com.educore.student.StudentAnswer;
import com.educore.question.Question;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuizSubmitMapper {

    // تحويل الطلب إلى محاولة (بدون حفظ)
    default StudentQuizAttempt toAttempt(SubmitQuizRequest request,Student student) {
        if (request == null) return null;

        return StudentQuizAttempt.builder()
                .student(student)
                .submitted(false)
                .build();
    }

    // تحويل الطلب إلى محاولة مع تحديد الاختبار
    default StudentQuizAttempt toAttemptWithQuiz(SubmitQuizRequest request, com.educore.quiz.Quiz quiz, Student student) {
        if (request == null || quiz == null) return null;

        return StudentQuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .submitted(false)
                .build();
    }

    // تحويل قائمة الإجابات إلى Map (باستخدام String)
    default Map<Long, String> toAnswerMap(SubmitQuizRequest request) {
        if (request == null || request.getAnswers() == null) return null;

        return request.getAnswers().stream()
                .collect(Collectors.toMap(
                        AnswerRequest::getQuestionId,
                        AnswerRequest::getSelectedAnswer,
                        (existing, replacement) -> {
//                            log.warn("Duplicate answer for question: {}", existing);
                            return existing;
                        }
                ));
    }

    // تحويل الطلب إلى قائمة StudentAnswer مع ربط الأسئلة
    default List<StudentAnswer> toStudentAnswers(
            SubmitQuizRequest request,
            StudentQuizAttempt attempt,
            Map<Long, Question> questionMap) { // نحتاج خريطة الأسئلة

        if (request == null || request.getAnswers() == null || attempt == null || questionMap == null)
            return null;

        return request.getAnswers().stream()
                .map(answer -> {
                    Question question = questionMap.get(answer.getQuestionId());
                    if (question == null) {
//                        log.warn("Question not found for id: {}", answer.getQuestionId());
                        return null;
                    }

                    return StudentAnswer.builder()
                            .attempt(attempt)
                            .question(question)
                            .selectedAnswer(answer.getSelectedAnswer())
                            .build();
                })
                .filter(answer -> answer != null)
                .collect(Collectors.toList());
    }

    // تحويل Map الإجابات إلى قائمة StudentAnswer
    default List<StudentAnswer> toStudentAnswersFromMap(
            Map<Long, String> answersMap,
            StudentQuizAttempt attempt,
            Map<Long, Question> questionMap) {

        if (answersMap == null || attempt == null || questionMap == null) return null;

        return answersMap.entrySet().stream()
                .map(entry -> {
                    Question question = questionMap.get(entry.getKey());
                    if (question == null) {
//                        log.warn("Question not found for id: {}", entry.getKey());
                        return null;
                    }

                    return StudentAnswer.builder()
                            .attempt(attempt)
                            .question(question)
                            .selectedAnswer(entry.getValue())
                            .build();
                })
                .filter(answer -> answer != null)
                .collect(Collectors.toList());
    }

    // التحقق من صحة الإجابات (عدد الأسئلة)
    default boolean validateAnswersCount(SubmitQuizRequest request, int expectedCount) {
        if (request == null || request.getAnswers() == null) return false;
        return request.getAnswers().size() == expectedCount;
    }

    // استخراج معرفات الأسئلة من الطلب
    default List<Long> extractQuestionIds(SubmitQuizRequest request) {
        if (request == null || request.getAnswers() == null) return List.of();

        return request.getAnswers().stream()
                .map(AnswerRequest::getQuestionId)
                .collect(Collectors.toList());
    }
}