package com.educore.assignment;

import com.educore.assignment.assignmentQuestion.AssignmentQuestion;
import com.educore.assignment.assignmentQuestion.StudentAssignmentAnswer;
import com.educore.assignment.assignmentQuestion.StudentAssignmentAnswerRepository;
import com.educore.common.CacheNames;
import com.educore.common.SortValidator;
import com.educore.dtocourse.mapper.*;
import com.educore.dtocourse.request.*;
import com.educore.dtocourse.response.*;
import com.educore.exception.ResourceNotFoundException;
import com.educore.exception.ResourceAlreadyExistsException;
import com.educore.exception.UnauthorizedException;
import com.educore.lesson.LessonRepository;
import com.educore.lesson.Week;
import com.educore.security.JwtUserPrincipal;
import com.educore.student.Student;
import com.educore.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final StudentAssignmentAttemptRepository attemptRepository;
    private final AssignmentCreateMapper createMapper;
    private final AssignmentMapper assignmentMapper;
    private final AssignmentSubmitMapper submitMapper;
    private final AssignmentResultMapper resultMapper;
    private final SortValidator sortValidator;
    private final LessonRepository weekRepository;
    private final StudentAssignmentAnswerRepository studentAnswerRepository;
    private final AssignmentValidator assignmentValidator;
    private final AssignmentScoreCalculator scoreCalculator;
    private final AssignmentAnswerMapper answerMapper;
    private final StudentRepository studentRepository;

    private static final List<String> ALLOWED_SORT_FIELDS =
            List.of("id", "title", "createdAt");
    private Student getCurrentStudent() {
        try {
            JwtUserPrincipal principal = (JwtUserPrincipal) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            Long userId = principal.getUserId();

            // جلب الطالب من قاعدة البيانات
            return studentRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("الطالب غير موجود"));
        } catch (Exception e) {
            log.error("Failed to get current student: {}", e.getMessage());
            throw new UnauthorizedException("غير مصرح لك بالوصول");
        }
    }

    private Long getCurrentStudentId() {
        return getCurrentStudent().getId();
    }
    // ================= CREATE ASSIGNMENT =================

    @CacheEvict(value = {CacheNames.ASSIGNMENTS_BY_WEEK}, allEntries = true)
    @Transactional
    public AssignmentResponse createAssignment(CreateAssignmentRequest request) {
        log.info("Creating assignment with title: {}", request.getTitle());

        Week week = weekRepository.findById(request.getWeekId())
                .orElseThrow(() -> new ResourceNotFoundException("الأسبوع غير موجود"));

        Assignment assignment = createMapper.toEntity(request);
        assignment.setWeek(week);

        Assignment saved = assignmentRepository.save(assignment);
        log.info("Assignment created successfully with id: {}", saved.getId());

        return assignmentMapper.toResponse(saved);
    }

    // ================= GET BY ID =================

    @Cacheable(value = CacheNames.ASSIGNMENTS, key = "#assignmentId")
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignment(Long assignmentId) {
        log.info("Fetching assignment with id: {}", assignmentId);

        Assignment assignment = assignmentRepository.findWithQuestionsById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("الواجب غير موجود بالرقم: " + assignmentId));

        return assignmentMapper.toResponse(assignment);
    }

    // ================= LIST BY WEEK =================

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.ASSIGNMENTS_BY_WEEK,
            key = "#weekId + '-' + #pageable.pageNumber")
    public Page<AssignmentResponse> getAssignmentsByWeek(Long weekId, Pageable pageable) {
        sortValidator.validate(pageable, ALLOWED_SORT_FIELDS);
        log.info("Fetching assignments for week: {} page: {}", weekId, pageable.getPageNumber());

        Page<Assignment> page = assignmentRepository.findByWeekId(weekId, pageable);
        return page.map(assignmentMapper::toResponse);
    }

    // ================= SUBMIT ASSIGNMENT =================

    @Transactional(rollbackFor = Exception.class)
    public AssignmentResultResponse submitAssignment(Long assignmentId, SubmitAssignmentRequest request) {
        Student student = getCurrentStudent();  // 👈 جلب الطالب من SecurityContext
        Long studentId = student.getId();

        // 1. التحقق من صحة الطلب (Validator)
        assignmentValidator.validateSubmitRequest(request, assignmentId);

        // 2. جلب الواجب مع الأسئلة
        Assignment assignment = assignmentRepository
                .findWithQuestionsByIdAndDeletedFalse(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("الواجب غير موجود: " + assignmentId));

        // 3. التحقق من الموعد النهائي (Deadline)
        if (assignmentValidator.isAssignmentExpired(assignment)) {
            throw new IllegalStateException("انتهى الموعد النهائي لتسليم هذا الواجب");
        }

        // 4. جلب أو إنشاء المحاولة مع قفل Pessimistic
        StudentAssignmentAttempt attempt = attemptRepository
                .findByAssignmentAndStudentWithLock(assignmentId, student)
                .orElseGet(() -> StudentAssignmentAttempt.builder()
                        .assignment(assignment)
                        .student(student)
                        .submitted(false)
                        .score(0)
                        .build());

        // 5. التحقق من أن المحاولة لم تُسلم بعد
        if (Boolean.TRUE.equals(attempt.getSubmitted())) {
            throw new IllegalStateException("تم تسليم هذا الواجب بالفعل");
        }

        // 6. تحويل الإجابات وحساب الدرجة
        Map<Long, String> answers = submitMapper.toAnswerMap(request);
        assignmentValidator.validateAllQuestionsAnswered(assignment, answers);

        AssignmentScoreCalculator.AssignmentScoreResult result = scoreCalculator.calculateScore(assignment, answers);

        // 7. تحديث المحاولة
        attempt.setScore(result.getScore());
        attempt.setSubmitted(true);
        attempt.setSubmittedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        // 8. حفظ إجابات الطالب للرجوع إليها
        Map<Long, AssignmentQuestion> questionMap = assignment.getQuestions().stream()
                .collect(Collectors.toMap(AssignmentQuestion::getId, q -> q));

        List<StudentAssignmentAnswer> studentAnswers = answerMapper.toStudentAnswersWithQuestions(attempt, answers, questionMap);
        studentAnswerRepository.saveAll(studentAnswers);

        log.info("تم تسليم الواجب - الطالب: {}, الواجب: {}, الدرجة: {}/{}",
                studentId, assignmentId, result.getScore(), result.getTotalMarks());

        return resultMapper.toDetailResponse(attempt, assignment);
    }

    // ================= DELETE ASSIGNMENT =================

    @Transactional
    @CacheEvict(value = {CacheNames.ASSIGNMENTS, CacheNames.ASSIGNMENTS_BY_WEEK}, allEntries = true)
    public void deleteAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("الواجب غير موجود بالرقم: " + assignmentId));

        assignment.setDeleted(true);
        assignment.setActive(false);

        log.info("تم حذف الواجب رقم {} بشكل ناعم", assignmentId);
    }
}