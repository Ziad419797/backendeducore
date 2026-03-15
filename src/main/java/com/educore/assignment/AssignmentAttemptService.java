package com.educore.assignment;

import com.educore.assignment.assignmentQuestion.StudentAssignmentAnswer;
import com.educore.assignment.assignmentQuestion.StudentAssignmentAnswerRepository;
import com.educore.common.SortValidator;
import com.educore.dtocourse.mapper.AssignmentAnswerMapper;
import com.educore.exception.ResourceNotFoundException;
import com.educore.assignment.assignmentQuestion.AssignmentQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentAttemptService {

    private final StudentAssignmentAttemptRepository attemptRepository;
    private final AssignmentRepository assignmentRepository;
    private final SortValidator sortValidator;
    private final StudentAssignmentAnswerRepository studentAnswerRepository;
    private final AssignmentAnswerMapper answerMapper;

    // تم إضافة الـ Fields الخاصة بالتحقق والحساب
    private final AssignmentValidator assignmentValidator;
    private final AssignmentScoreCalculator scoreCalculator;

    private static final List<String> ALLOWED_SORT_FIELDS =
            List.of("id", "score", "submittedAt");

    // ================= GET ATTEMPT BY ID =================

    @Transactional(readOnly = true)
    public StudentAssignmentAttempt getAttemptById(Long attemptId) {
        log.info("Fetching assignment attempt with id: {}", attemptId);

        return attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("محاولة الواجب غير موجودة بالرقم: " + attemptId));
    }

    // ================= GET ATTEMPT BY STUDENT AND ASSIGNMENT =================

    @Transactional(readOnly = true)
    public StudentAssignmentAttempt getAttemptByStudentAndAssignment(Long studentId, Long assignmentId) {
        log.info("Fetching assignment attempt for student: {} and assignment: {}", studentId, assignmentId);

        return attemptRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("لم يتم العثور على محاولة للطالب %d في الواجب %d", studentId, assignmentId)
                ));
    }

    // ================= GET ALL ATTEMPTS BY STUDENT =================

    @Transactional(readOnly = true)
    public Page<StudentAssignmentAttempt> getAttemptsByStudent(Long studentId, Pageable pageable) {
        sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);

        log.info("Fetching assignment attempts for student: {}, page: {}", studentId, pageable.getPageNumber());

        Page<StudentAssignmentAttempt> page = attemptRepository.findByStudentId(studentId, pageable);

        if (page.isEmpty()) {
            log.warn("No assignment attempts found for student: {}", studentId);
        }

        return page;
    }

    // ================= GET ALL ATTEMPTS BY ASSIGNMENT =================

    @Transactional(readOnly = true)
    public Page<StudentAssignmentAttempt> getAttemptsByAssignment(Long assignmentId, Pageable pageable) {
        sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);

        log.info("Fetching attempts for assignment: {}, page: {}", assignmentId, pageable.getPageNumber());

        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("الواجب غير موجود بالرقم: " + assignmentId);
        }

        Page<StudentAssignmentAttempt> page = attemptRepository.findByAssignmentId(assignmentId, pageable);

        if (page.isEmpty()) {
            log.warn("No attempts found for assignment: {}", assignmentId);
        }

        return page;
    }

    // ================= GET ASSIGNMENT STATISTICS =================

    @Transactional(readOnly = true)
    public AssignmentStatistics getAssignmentStatistics(Long assignmentId) {
        log.info("جلب إحصائيات الواجب: {}", assignmentId);

        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("الواجب غير موجود بالرقم: " + assignmentId);
        }

        Double averageScore = attemptRepository.getAverageScoreByAssignmentId(assignmentId);
        Long totalSubmissions = attemptRepository.countSubmittedByAssignmentId(assignmentId);

        return AssignmentStatistics.builder()
                .assignmentId(assignmentId)
                .averageScore(averageScore != null ? averageScore : 0.0)
                .totalSubmissions(totalSubmissions != null ? totalSubmissions : 0)
                .build();
    }

    // ================= GET STUDENT ANSWERS =================

    @Transactional(readOnly = true)
    public List<StudentAssignmentAnswer> getStudentAnswers(Long attemptId) {
        log.info("جلب إجابات الطالب لمحاولة الواجب: {}", attemptId);

        if (!attemptRepository.existsById(attemptId)) {
            throw new ResourceNotFoundException("محاولة الواجب غير موجودة: " + attemptId);
        }

        return studentAnswerRepository.findByAttemptId(attemptId);
    }

    // ================= DELETE ATTEMPT =================

    @Transactional
    public void deleteAttempt(Long attemptId) {
        log.info("حذف محاولة الواجب رقم: {}", attemptId);

        StudentAssignmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("المحاولة غير موجودة: " + attemptId));

        // حذف الإجابات التفصيلية أولاً
        studentAnswerRepository.deleteByAttemptId(attemptId);

        // حذف سجل المحاولة
        attemptRepository.delete(attempt);

        log.info("تم حذف المحاولة {} بنجاح", attemptId);
    }

    // ================= HELPER: SAVE ANSWERS =================

    private void saveStudentAnswers(StudentAssignmentAttempt attempt, Map<Long, String> answers, Assignment assignment) {
        // إنشاء Map للأسئلة لربط الإجابة بالكائن الكامل
        Map<Long, AssignmentQuestion> questionMap = assignment.getQuestions().stream()
                .collect(Collectors.toMap(AssignmentQuestion::getId, q -> q));

        List<StudentAssignmentAnswer> studentAnswers = answerMapper.toStudentAnswersWithQuestions(attempt, answers, questionMap);

        if (studentAnswers != null && !studentAnswers.isEmpty()) {
            studentAnswerRepository.saveAll(studentAnswers);
        }
    }

    // ================= INNER CLASS FOR STATISTICS =================

    @lombok.Builder
    @lombok.Getter
    @lombok.Setter
    public static class AssignmentStatistics {
        private Long assignmentId;
        private Double averageScore;
        private Long totalSubmissions;
    }
}