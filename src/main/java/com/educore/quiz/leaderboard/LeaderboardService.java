package com.educore.quiz.leaderboard;

import com.educore.common.CacheNames;
import com.educore.dtocourse.mapper.LeaderboardMapper;
import com.educore.dtocourse.response.LeaderboardEntryResponse;
import com.educore.dtocourse.response.LeaderboardResponse;
import com.educore.exception.ResourceNotFoundException;
import com.educore.quiz.Quiz;
import com.educore.quiz.QuizRepository;
import com.educore.quiz.StudentQuizAttempt;
import com.educore.quiz.StudentQuizAttemptRepository;
import com.educore.security.JwtUserPrincipal;
import com.educore.student.Student;
import com.educore.student.StudentRepository;
import com.educore.course.CourseRepository;
import com.educore.course.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    private final StudentQuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final LeaderboardMapper leaderboardMapper;

    // ==================== QUIZ LEADERBOARD ====================

    /**
     * أفضل 10 طلاب في كويز معين
     */
    @Cacheable(value = CacheNames.LEADERBOARD_QUIZ, key = "#quizId")
    @Transactional(readOnly = true)
    public LeaderboardResponse getQuizLeaderboard(Long quizId) {
        log.info("جلب أفضل الطلاب للكويز: {}", quizId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("الاختبار غير موجود"));

        Pageable topTen = PageRequest.of(0, 10);
        List<StudentQuizAttempt> topAttempts = attemptRepository
                .findTop10ByQuizIdWithStudent(quizId, topTen);

        // إحصائيات الكويز
        List<Object[]> statsList = attemptRepository.getQuizStats(quizId);
        Integer highestScore = 0;
        Integer lowestScore = 0;
        Double averageScore = 0.0;
        Long totalParticipants = 0L;

        // التأكد إن فيه نتائج رجعت (مش فاضية)
        if (statsList != null && !statsList.isEmpty()) {
            Object[] stats = statsList.get(0); // أول صف هو اللي فيه الإحصائيات

            highestScore = stats[0] != null ? ((Number) stats[0]).intValue() : 0;
            lowestScore = stats[1] != null ? ((Number) stats[1]).intValue() : 0;
            averageScore = stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;
            totalParticipants = stats[3] != null ? ((Number) stats[3]).longValue() : 0;
        }
        List<LeaderboardEntryResponse> entries = leaderboardMapper.toQuizEntryList(topAttempts, 1);

        return leaderboardMapper.toResponse(
                "أفضل الطلاب في اختبار: " + quiz.getTitle(),
                "QUIZ",
                quizId,
                entries,
                totalParticipants.intValue(),
                averageScore,
                highestScore,
                lowestScore
        );
    }

    // ==================== COURSE LEADERBOARD ====================

    /**
     * أفضل 10 طلاب في كورس معين
     */
    @Cacheable(value = CacheNames.LEADERBOARD_COURSE, key = "#courseId")
    @Transactional(readOnly = true)
    public LeaderboardResponse getCourseLeaderboard(Long courseId) {
        log.info("جلب أفضل الطلاب للكورس: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("الكورس غير موجود"));

        Pageable topTen = PageRequest.of(0, 10);
        List<Object[]> topStudents = attemptRepository.findTopStudentsByCourse(courseId, topTen);

        // جمع كل studentIds
        List<Long> studentIds = topStudents.stream()
                .map(row -> ((Number) row[0]).longValue())
                .collect(Collectors.toList());

        // جلب بيانات الطلاب دفعة واحدة
        Map<Long, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        List<LeaderboardEntryResponse> entries = IntStream.range(0, topStudents.size())
                .mapToObj(i -> {
                    Object[] row = topStudents.get(i);
                    Long studentId = ((Number) row[0]).longValue();
                    Integer totalScore = ((Number) row[1]).intValue();
                    Double avgScore = ((Number) row[2]).doubleValue();
                    Student student = studentMap.get(studentId);

                    return leaderboardMapper.toCourseEntry(
                            student,
                            course,
                            totalScore,
                            avgScore,
                            i + 1
                    );
                })
                .collect(Collectors.toList());

        return LeaderboardResponse.builder()
                .title("أفضل الطلاب في كورس: " + course.getTitle())
                .type("COURSE")
                .entityId(courseId)
                .entries(entries)
                .totalParticipants(entries.size())
                .build();
    }
    // ==================== GLOBAL LEADERBOARD ====================

    /**
     * أفضل 10 طلاب في النظام ككل
     */
    @Cacheable(value = CacheNames.LEADERBOARD_GLOBAL, key = "#page + '-' + #size")
    @Transactional(readOnly = true)
    public LeaderboardResponse getGlobalLeaderboard(int page, int size) {
        log.info("جلب أفضل الطلاب في النظام");
        Pageable pageable = PageRequest.of(page, size);
         List<Object[]> topStudents = attemptRepository.findTopStudentsGlobally(pageable);
        // جمع كل studentIds
        List<Long> studentIds = topStudents.stream()
                .map(row -> ((Number) row[0]).longValue())
                .collect(Collectors.toList());

        // جلب بيانات الطلاب دفعة واحدة
        Map<Long, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        int startRank = (page * size) + 1;
        List<LeaderboardEntryResponse> entries = IntStream.range(0, topStudents.size())
                .mapToObj(i -> {
                    Object[] row = topStudents.get(i);
                    Long studentId = ((Number) row[0]).longValue();
                    Integer totalScore = ((Number) row[1]).intValue();
                    Double avgScore = ((Number) row[2]).doubleValue();
                    Integer totalPossible = ((Number) row[4]).intValue();
                    Student student = studentMap.get(studentId);

                    return leaderboardMapper.toGlobalEntry(
                            student,
                            totalScore,
                            avgScore,
                            totalPossible,
                            startRank + i
                    );
                })
                .collect(Collectors.toList());

        return LeaderboardResponse.builder()
                .title("أفضل الطلاب في النظام")
                .type("GLOBAL")
                .entries(entries)
                .totalParticipants(entries.size())
                .build();
    }
    // ==================== STUDENT RANK ====================

    /**
     * ترتيب الطالب الحالي في كويز معين
     */
    @Transactional(readOnly = true)
    public Integer getMyRankInQuiz(Long quizId, JwtUserPrincipal principal) {

        Long currentStudentId = principal.getUserId();
        log.info("جلب ترتيب الطالب {} في الكويز {}", currentStudentId, quizId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("الاختبار غير موجود"));
        StudentQuizAttempt attempt = attemptRepository
                .findByQuizIdAndStudentId(quizId, currentStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("لا توجد محاولة"));

        return attemptRepository.getStudentRankInQuiz(quizId, attempt.getScore());
    }
}