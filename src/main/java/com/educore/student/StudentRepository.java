package com.educore.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByParentPhone(String parentPhone);

    Optional<Student> findByPhone(String phone);
//    Optional<Student> findByUserId(Long userId);
// ✅ ده الصح
Optional<Student> findById(Long id);
    Optional<Student> findByStudentCode(String studentCode);
    boolean existsByStudentCode(String studentCode);
    // جلب الطلاب حسب الحالة مع دعم الصفحة
    Page<Student> findByStatus(StudentStatus status, Pageable pageable);

    // للبحث عن طالب معين برقم تليفونه أو كوده في قائمة الانتظار
    Optional<Student> findByPhoneOrStudentCode(String phone, String studentCode);
}