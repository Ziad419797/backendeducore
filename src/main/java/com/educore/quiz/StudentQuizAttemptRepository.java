package com.educore.quiz;

import com.educore.student.Student;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentQuizAttemptRepository extends JpaRepository<StudentQuizAttempt, Long> {

    // ==================== الطرق الآمنة (باستخدام Student object) ====================
    // هذه هي الطرق المفضلة للأمان والاتساق مع الـ JPA

    /**
     * جلب محاولة بواسطة الكويز والطالب (كائن)
     */
    Optional<StudentQuizAttempt> findByQuizAndStudent(Quiz quiz, Student student);

    /**
     * التحقق من وجود محاولة بواسطة الكويز والطالب
     */
    boolean existsByQuizAndStudent(Quiz quiz, Student student);

    /**
     * جلب جميع محاولات طالب معين (باستخدام كائن Student)
     */
    Page<StudentQuizAttempt> findByStudent(Student student, Pageable pageable);

    /**
     * جلب محاولة مع قفل Pessimistic (باستخدام Student)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM StudentQuizAttempt a WHERE a.quiz.id = :quizId AND a.student = :student")
    Optional<StudentQuizAttempt> findByQuizAndStudentWithLock(
            @Param("quizId") Long quizId,
            @Param("student") Student student
    );

    // ==================== طرق التوافق (باستخدام studentId) ====================
    // هذه الطرق موجودة للتوافق مع الكود القديم أو الحالات التي نحتاج فيها ID فقط

    /**
     * جلب محاولة بواسطة معرف الكويز ومعرف الطالب
     */
    Optional<StudentQuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);

    /**
     * التحقق من وجود محاولة بواسطة معرف الكويز ومعرف الطالب
     */
    boolean existsByQuizIdAndStudentId(Long quizId, Long studentId);

    /**
     * جلب محاولة غير مسلمة بواسطة معرف الكويز ومعرف الطالب
     */
    Optional<StudentQuizAttempt> findByQuizIdAndStudentIdAndSubmittedFalse(
            Long quizId,
            Long studentId
    );

    /**
     * جلب جميع محاولات طالب معين (باستخدام studentId)
     */
    Page<StudentQuizAttempt> findByStudentId(Long studentId, Pageable pageable);

    /**
     * جلب جميع محاولات كويز معين
     */
    Page<StudentQuizAttempt> findByQuizId(Long quizId, Pageable pageable);

    /**
     * جلب محاولات طالب معين حسب حالة التسليم
     */
    Page<StudentQuizAttempt> findByStudentIdAndSubmitted(
            Long studentId,
            Boolean submitted,
            Pageable pageable
    );

    /**
     * جلب محاولة مع قفل Pessimistic (باستخدام studentId) - للتوافق
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM StudentQuizAttempt a WHERE a.quiz.id = :quizId AND a.student.id = :studentId")
    Optional<StudentQuizAttempt> findByQuizIdAndStudentIdWithLock(
            @Param("quizId") Long quizId,
            @Param("studentId") Long studentId
    );

    // ==================== طرق الاستعلامات المتقدمة ====================

    /**
     * التحقق من وجود محاولات نشطة لكويز معين
     */
    @Query("SELECT COUNT(a) > 0 FROM StudentQuizAttempt a WHERE a.quiz.id = :quizId AND a.submitted = false")
    boolean existsByQuizIdAndSubmittedFalse(@Param("quizId") Long quizId);

    /**
     * حذف جميع محاولات كويز معين
     */
    @Modifying
    @Query("DELETE FROM StudentQuizAttempt a WHERE a.quiz.id = :quizId")
    void deleteByQuizId(@Param("quizId") Long quizId);

    /**
     * حساب متوسط الدرجات لكويز معين
     */
    @Query("SELECT AVG(a.score) FROM StudentQuizAttempt a WHERE a.quiz.id = :quizId AND a.submitted = true")
    Double getAverageScoreByQuizId(@Param("quizId") Long quizId);

    /**
     * حساب عدد المحاولات المسلمة لكويز معين
     */
    @Query("SELECT COUNT(a) FROM StudentQuizAttempt a WHERE a.quiz.id = :quizId AND a.submitted = true")
    Long countSubmittedByQuizId(@Param("quizId") Long quizId);

    /**
     * جلب أحدث محاولة لطالب في كويز معين
     */
    Optional<StudentQuizAttempt> findTopByQuizIdAndStudentIdOrderByStartedAtDesc(
            Long quizId,
            Long studentId
    );

    /**
     * جلب جميع المحاولات المنتهية (expired) التي لم تُسلم
     */
    @Query("SELECT a FROM StudentQuizAttempt a WHERE a.expiresAt < CURRENT_TIMESTAMP AND a.submitted = false")
    List<StudentQuizAttempt> findAllExpiredAndNotSubmitted();

    // ==================== LEADERBOARD METHODS ====================

    /**
     * أفضل 10 طلاب في كويز معين
     */
    @Query("SELECT a FROM StudentQuizAttempt a " +
            "WHERE a.quiz.id = :quizId " +
            "AND a.submitted = true " +
            "ORDER BY a.score DESC, a.submittedAt ASC")
    List<StudentQuizAttempt> findTop10ByQuizIdOrderByScoreDesc(
            @Param("quizId") Long quizId,
            Pageable pageable
    );

    /**
     * أفضل 10 طلاب في كويز معين مع إحصائيات
     */
    @Query("SELECT a FROM StudentQuizAttempt a " +
            "JOIN FETCH a.student s " +
            "WHERE a.quiz.id = :quizId " +
            "AND a.submitted = true " +
            "ORDER BY a.score DESC, a.submittedAt ASC")
    List<StudentQuizAttempt> findTop10ByQuizIdWithStudent(
            @Param("quizId") Long quizId,
            Pageable pageable
    );




    /**
     * ترتيب الطالب في كويز معين
     */
    @Query("SELECT COUNT(a) + 1 FROM StudentQuizAttempt a " +
            "WHERE a.quiz.id = :quizId " +
            "AND a.submitted = true " +
            "AND a.score > :score")
    Integer getStudentRankInQuiz(
            @Param("quizId") Long quizId,
            @Param("score") Integer score
    );

    /**
     * إحصائيات الكويز
     */
//    @Query("SELECT MAX(a.score), MIN(a.score), AVG(a.score), COUNT(a) " +
//            "FROM StudentQuizAttempt a " +
//            "WHERE a.quiz.id = :quizId AND a.submitted = true")
//    Object[] getQuizStats(@Param("quizId") Long quizId);
    @Query("SELECT MAX(a.score), MIN(a.score), AVG(a.score), COUNT(a) " +
            "FROM StudentQuizAttempt a " +
            "WHERE a.quiz.id = :quizId AND a.submitted = true")
    List<Object[]> getQuizStats(@Param("quizId") Long quizId);

    /**
     * أفضل 10 طلاب في كورس معين
     * المسار: Attempt -> Quiz -> Week -> Sessions (List) -> Courses (List)
     */
    @Query("SELECT a.student.id, " +
            "SUM(a.score), " +
            "AVG(a.score * 100.0 / COALESCE((SELECT SUM(q_inner.mark) FROM Question q_inner WHERE q_inner.quiz = a.quiz), 1)), " +
            "COUNT(a) " +
            "FROM StudentQuizAttempt a " +
            "JOIN a.quiz q " +
            "JOIN q.week w " +
            "JOIN w.sessions s " +    // نفتح باب السيشنز
            "JOIN s.courses c " +     // نفتح باب الكورسات اللي جوه السيشن (اسمها courses عندك)
            "WHERE c.id = :courseId " +
            "AND a.submitted = true " +
            "GROUP BY a.student.id " +
            "ORDER BY SUM(a.score) DESC")
    List<Object[]> findTopStudentsByCourse(@Param("courseId") Long courseId, Pageable pageable);
    /**
     * أفضل 10 طلاب في النظام ككل
     * تحسب: [ID الطالب, مجموع درجاته، متوسط نسبته المئوية، عدد المحاولات، إجمالي الدرجات الممكنة]
     */
    @Query("SELECT a.student.id, " +
            "SUM(a.score), " +
            "AVG(a.score * 100.0 / (SELECT COALESCE(SUM(q.mark), 1) FROM Question q WHERE q.quiz = a.quiz)), " +
            "COUNT(a), " +
            "SUM((SELECT SUM(q2.mark) FROM Question q2 WHERE q2.quiz = a.quiz)) " +
            "FROM StudentQuizAttempt a " +
            "WHERE a.submitted = true " +
            "GROUP BY a.student.id " +
            "ORDER BY SUM(a.score) DESC")
    List<Object[]> findTopStudentsGlobally(Pageable pageable);
}