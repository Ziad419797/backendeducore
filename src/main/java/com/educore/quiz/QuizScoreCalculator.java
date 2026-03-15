package com.educore.quiz;

import com.educore.question.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QuizScoreCalculator {

    /**
     * حساب درجة الطالب في الاختبار
     */
    public ScoreCalculationResult calculateScore(
            Quiz quiz,
            Map<Long, String> answers) { // 👈 تم التغيير لـ String ليتناسب مع التعديل الجديد

        int score = 0;
        int totalMarks = 0;

        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                // إضافة درجة السؤال لإجمالي الدرجات
                int questionMark = (q.getMark() != null) ? q.getMark() : 0;
                totalMarks += questionMark;

                // مقارنة إجابة الطالب (String) مع الإجابة الصحيحة (String)
                String selected = answers.get(q.getId());

                // استخدام الدالة المساعدة اللي إنتي عملتيها جوه كلاس Question
                if (q.isCorrectAnswer(selected)) {
                    score += questionMark;
                }
            }
        }

        return new ScoreCalculationResult(score, totalMarks);
    }

    /**
     * حساب إجمالي درجات الاختبار
     */
    public int calculateTotalMarks(Quiz quiz) {
        if (quiz.getQuestions() == null) return 0;
        return quiz.getQuestions().stream()
                .mapToInt(q -> (q.getMark() != null) ? q.getMark() : 0)
                .sum();
    }

    @Getter
    @AllArgsConstructor
    public static class ScoreCalculationResult {
        private final int score;
        private final int totalMarks;

        public double getPercentage() {
            if (totalMarks == 0) return 0.0;
            return ((double) score / totalMarks) * 100;
        }
    }
}