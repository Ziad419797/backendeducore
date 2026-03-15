package com.educore.assignment;

import com.educore.assignment.assignmentQuestion.AssignmentQuestion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AssignmentScoreCalculator {

    /**
     * حساب درجة الطالب في الواجب
     */
    public AssignmentScoreResult calculateScore(Assignment assignment, Map<Long, String> answers) {
        int score = 0;
        int totalMarks = 0;

        if (assignment.getQuestions() != null) {
            for (AssignmentQuestion q : assignment.getQuestions()) {
                int questionMark = (q.getMark() != null) ? q.getMark() : 0;
                totalMarks += questionMark;

                String selected = answers.get(q.getId());

                // مقارنة إجابة الطالب المكتوبة مع الإجابة الصحيحة المسجلة
                if (selected != null && selected.equalsIgnoreCase(q.getCorrectAnswer())) {
                    score += questionMark;
                }
            }
        }

        return new AssignmentScoreResult(score, totalMarks);
    }

    @Getter
    @AllArgsConstructor
    public static class AssignmentScoreResult {
        private final int score;
        private final int totalMarks;

        public double getPercentage() {
            if (totalMarks == 0) return 0.0;
            return ((double) score / totalMarks) * 100;
        }
    }
}