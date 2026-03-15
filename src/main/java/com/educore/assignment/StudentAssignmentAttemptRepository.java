package com.educore.assignment;

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
public interface StudentAssignmentAttemptRepository extends JpaRepository<StudentAssignmentAttempt, Long> {

    // ==================== الطرق الآمنة (باستخدام Student object) ====================

    /**
     * جلب محاولة بواسطة الواجب والطالب (كائن)
     */
    Optional<StudentAssignmentAttempt> findByAssignmentAndStudent(Assignment assignment, Student student);

    /**
     * التحقق من وجود محاولة بواسطة الواجب والطالب
     */
    boolean existsByAssignmentAndStudent(Assignment assignment, Student student);

    /**
     * جلب جميع محاولات طالب معين (باستخدام كائن Student)
     */
    Page<StudentAssignmentAttempt> findByStudent(Student student, Pageable pageable);

    /**
     * جلب محاولة مع قفل Pessimistic (باستخدام Student)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM StudentAssignmentAttempt a WHERE a.assignment.id = :assignmentId AND a.student = :student")
    Optional<StudentAssignmentAttempt> findByAssignmentAndStudentWithLock(
            @Param("assignmentId") Long assignmentId,
            @Param("student") Student student
    );

    // ==================== طرق التوافق (باستخدام studentId) ====================

    /**
     * جلب محاولة بواسطة معرف الواجب ومعرف الطالب
     */
    Optional<StudentAssignmentAttempt> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    /**
     * التحقق من وجود محاولة بواسطة معرف الواجب ومعرف الطالب
     */
    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    /**
     * جلب محاولة غير مسلمة بواسطة معرف الواجب ومعرف الطالب
     */
    Optional<StudentAssignmentAttempt> findByAssignmentIdAndStudentIdAndSubmittedFalse(
            Long assignmentId,
            Long studentId
    );

    /**
     * جلب جميع محاولات طالب معين (باستخدام studentId)
     */
    Page<StudentAssignmentAttempt> findByStudentId(Long studentId, Pageable pageable);

    /**
     * جلب جميع محاولات واجب معين
     */
    Page<StudentAssignmentAttempt> findByAssignmentId(Long assignmentId, Pageable pageable);

    /**
     * جلب محاولات طالب معين حسب حالة التسليم
     */
    Page<StudentAssignmentAttempt> findByStudentIdAndSubmitted(
            Long studentId,
            Boolean submitted,
            Pageable pageable
    );

    /**
     * جلب محاولة مع قفل Pessimistic (باستخدام studentId)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM StudentAssignmentAttempt a WHERE a.assignment.id = :assignmentId AND a.student.id = :studentId")
    Optional<StudentAssignmentAttempt> findByAssignmentIdAndStudentIdWithLock(
            @Param("assignmentId") Long assignmentId,
            @Param("studentId") Long studentId
    );

    // ==================== طرق الاستعلامات المتقدمة ====================

    /**
     * التحقق من وجود محاولات نشطة لواجب معين
     */
    @Query("SELECT COUNT(a) > 0 FROM StudentAssignmentAttempt a WHERE a.assignment.id = :assignmentId AND a.submitted = false")
    boolean existsByAssignmentIdAndSubmittedFalse(@Param("assignmentId") Long assignmentId);

    /**
     * حذف جميع محاولات واجب معين
     */
    @Modifying
    @Query("DELETE FROM StudentAssignmentAttempt a WHERE a.assignment.id = :assignmentId")
    void deleteByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * حساب متوسط الدرجات لواجب معين (بعد تصحيح المدرس)
     */
    @Query("SELECT AVG(a.score) FROM StudentAssignmentAttempt a WHERE a.assignment.id = :assignmentId AND a.submitted = true AND a.score IS NOT NULL")
    Double getAverageScoreByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * حساب عدد المحاولات المسلمة لواجب معين
     */
    @Query("SELECT COUNT(a) FROM StudentAssignmentAttempt a WHERE a.assignment.id = :assignmentId AND a.submitted = true")
    Long countSubmittedByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * جلب أحدث تسليم لطالب في واجب معين
     */
    Optional<StudentAssignmentAttempt> findTopByAssignmentIdAndStudentIdOrderBySubmittedAtDesc(
            Long assignmentId,
            Long studentId
    );
}