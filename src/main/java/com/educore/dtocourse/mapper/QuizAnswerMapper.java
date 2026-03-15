package com.educore.dtocourse.mapper;

import com.educore.question.Question;
import com.educore.quiz.StudentQuizAttempt;
import com.educore.student.StudentAnswer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class QuizAnswerMapper {

    /**
     * تحويل Map الإجابات إلى قائمة StudentAnswer
     */
    public List<StudentAnswer> toStudentAnswers(
            StudentQuizAttempt attempt,
            Map<Long, String> answersMap) {

        if (answersMap == null || attempt == null) return null;

        return answersMap.entrySet().stream()
                .map(entry -> {
                    // نحتاج لإنشاء كائن Question بالمعرف فقط
                    // هذا يفترض أن لديك طريقة لجلب السؤال لاحقاً
                    Question question = new Question();
                    question.setId(entry.getKey());

                    return StudentAnswer.builder()
                            .attempt(attempt)
                            .question(question)
                            .selectedAnswer(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * تحويل Map الإجابات إلى قائمة StudentAnswer مع خريطة الأسئلة الكاملة
     */
    public List<StudentAnswer> toStudentAnswersWithQuestions(
            StudentQuizAttempt attempt,
            Map<Long, String> answersMap,
            Map<Long, Question> questionMap) {

        if (answersMap == null || attempt == null || questionMap == null) return null;

        return answersMap.entrySet().stream()
                .map(entry -> {
                    Question question = questionMap.get(entry.getKey());
                    if (question == null) return null;

                    return StudentAnswer.builder()
                            .attempt(attempt)
                            .question(question)
                            .selectedAnswer(entry.getValue())
                            .build();
                })
                .filter(answer -> answer != null)
                .collect(Collectors.toList());
    }
}