package com.educore.teacher;

import com.educore.dto.mapper.StudentMapper;
import com.educore.dto.response.StudentResponse;
import com.educore.exception.ResourceNotFoundException;
import com.educore.student.Student;
import com.educore.student.StudentRepository;
import com.educore.student.StudentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherStudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper; // 👈 إضافة المابر هنا

    // 1. عرض كل الطلاب المنتظرين (Pending) بتحويلهم لـ Response
    public Page<StudentResponse> getPendingStudents(Pageable pageable) {
        log.debug("Fetching pending students with pagination");

        // جلب الداتا كـ Entities ثم تحويلها باستخدام المابر
        return studentRepository.findByStatus(StudentStatus.PENDING, pageable)
                .map(studentMapper::toResponse); // 👈 دي اللي بتشيل الباسورد وتظبط الـ FullName
    }

    // 2. قبول الطالب
    @Transactional
    public void approveStudent(Long studentId, String teacherName) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("الطالب غير موجود برقم: " + studentId));

        log.info("Teacher [{}] is approving student code: {}", teacherName, student.getStudentCode());

        student.approve(teacherName);
        studentRepository.save(student);

        log.info("Student [{}] is now ACTIVE", student.getStudentCode());
    }

    // 3. رفض الطالب
    @Transactional
    public void rejectStudent(Long studentId, String reason, String teacherName) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("الطالب غير موجود"));

        log.info("Teacher [{}] rejected student [{}] for reason: {}", teacherName, student.getStudentCode(), reason);

        student.reject(teacherName, reason);
        studentRepository.save(student);
    }
}