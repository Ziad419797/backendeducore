package com.educore.quiz;

import com.educore.common.SortValidator;
import com.educore.dtocourse.mapper.QuizAnswerMapper;
import com.educore.dtocourse.request.AnswerRequest;
import com.educore.dtocourse.request.SubmitQuizRequest;
import com.educore.dtocourse.response.QuizResultResponse;
import com.educore.exception.ResourceNotFoundException;
import com.educore.question.AnswerOption;
import com.educore.question.Question;
import com.educore.student.StudentAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptService {

    private final StudentQuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final SortValidator sortValidator;
    private final StudentAnswerRepository studentAnswerRepository;
    private final QuizAnswerMapper answerMapper;
    private final QuizValidator quizValidator;
    private final QuizScoreCalculator scoreCalculator;
    private static final List<String> ALLOWED_SORT_FIELDS =
            List.of("id", "score", "startedAt", "submittedAt");

    // ================= GET ATTEMPT BY ID =================

    @Transactional(readOnly = true)
    public StudentQuizAttempt getAttemptById(Long attemptId) {
        log.info("Fetching attempt with id: {}", attemptId);

        return attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));
    }

    // ================= GET ATTEMPT BY STUDENT AND QUIZ =================

    @Transactional(readOnly = true)
    public StudentQuizAttempt getAttemptByStudentAndQuiz(Long studentId, Long quizId) {
        log.info("Fetching attempt for student: {} and quiz: {}", studentId, quizId);

        return attemptRepository.findByQuizIdAndStudentId(quizId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Attempt not found for student %d and quiz %d", studentId, quizId)
                ));
    }

    // ================= GET ALL ATTEMPTS BY STUDENT =================

    @Transactional(readOnly = true)
    public Page<StudentQuizAttempt> getAttemptsByStudent(Long studentId, Pageable pageable) {
        sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);

        log.info("Fetching attempts for student: {}, page: {}", studentId, pageable.getPageNumber());

        Page<StudentQuizAttempt> page = attemptRepository.findByStudentId(studentId, pageable);

        if (page.isEmpty()) {
            log.warn("No attempts found for student: {}", studentId);
        }

        return page;
    }

    // ================= GET ALL ATTEMPTS BY QUIZ =================

    @Transactional(readOnly = true)
    public Page<StudentQuizAttempt> getAttemptsByQuiz(Long quizId, Pageable pageable) {
        sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);

        log.info("Fetching attempts for quiz: {}, page: {}", quizId, pageable.getPageNumber());

        // التحقق من وجود الكويز
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }

        Page<StudentQuizAttempt> page = attemptRepository.findByQuizId(quizId, pageable);

        if (page.isEmpty()) {
            log.warn("No attempts found for quiz: {}", quizId);
        }

        return page;
    }

    // ================= GET ATTEMPTS BY STUDENT AND STATUS =================

    @Transactional(readOnly = true)
    public Page<StudentQuizAttempt> getAttemptsByStudentAndStatus(
            Long studentId,
            Boolean submitted,
            Pageable pageable
    ) {
        sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);

        log.info("Fetching {} attempts for student: {}, page: {}",
                submitted ? "submitted" : "pending", studentId, pageable.getPageNumber());

        Page<StudentQuizAttempt> page = attemptRepository.findByStudentIdAndSubmitted(studentId, submitted, pageable);

        return page;
    }

    // ================= GET QUIZ STATISTICS =================

    @Transactional(readOnly = true)
    public QuizStatistics  getQuizStatistics(Long quizId) {
        log.info("جلب إحصائيات الاختبار: {}", quizId);

        // التحقق من وجود الكويز
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("الاختبار غير موجود بالرقم: " + quizId);
        }

        Double averageScore = attemptRepository.getAverageScoreByQuizId(quizId);
        Long totalSubmissions = attemptRepository.countSubmittedByQuizId(quizId);
        boolean hasActiveAttempts = attemptRepository.existsByQuizIdAndSubmittedFalse(quizId);


        return QuizStatistics.builder()
                .quizId(quizId)
                .averageScore(averageScore != null ? averageScore : 0.0)
                .totalSubmissions(totalSubmissions != null ? totalSubmissions : 0)
                .hasActiveAttempts(hasActiveAttempts)
                .build();
    }
    // ================= GET STUDENT ANSWERS =================

    @Transactional(readOnly = true)
    public List<StudentAnswer> getStudentAnswers(Long attemptId) {
        log.info("جلب إجابات الطالب للمحاولة: {}", attemptId);

        // التحقق من وجود المحاولة
        if (!attemptRepository.existsById(attemptId)) {
            throw new ResourceNotFoundException("المحاولة غير موجودة بالرقم: " + attemptId);
        }

        return studentAnswerRepository.findByAttemptId(attemptId);
    }

    // ================= DELETE ATTEMPT (ADMIN ONLY) =================

    @Transactional
    public void deleteAttempt(Long attemptId) {
        log.info("حذف المحاولة بالرقم: {}", attemptId);

        StudentQuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("المحاولة غير موجودة بالرقم: " + attemptId));

        // حذف الإجابات المرتبطة أولاً
        studentAnswerRepository.deleteByAttemptId(attemptId);

        // ثم حذف المحاولة
        attemptRepository.delete(attempt);

        log.info("تم حذف المحاولة {} بنجاح", attemptId);
    }

    private void saveStudentAnswers(StudentQuizAttempt attempt, Map<Long, String> answers, Quiz quiz) {
        // إنشاء خريطة الأسئلة للمساعدة في الربط
        Map<Long, Question> questionMap = quiz.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<StudentAnswer> studentAnswers = answerMapper.toStudentAnswersWithQuestions(attempt, answers, questionMap);

        if (studentAnswers != null && !studentAnswers.isEmpty()) {
            studentAnswerRepository.saveAll(studentAnswers);
        }
    }

    // ================= INNER CLASSES =================

    @lombok.Builder
    @lombok.Getter
    @lombok.Setter
    public static class QuizStatistics {
        private Long quizId;
        private Double averageScore;
        private Long totalSubmissions;
        private boolean hasActiveAttempts;
    }
//
//    @lombok.Getter
//    @lombok.AllArgsConstructor
//    private static class ScoreCalculationResult {
//        private final int score;
//        private final int totalMarks;
//    }
}