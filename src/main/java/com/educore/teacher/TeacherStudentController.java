package com.educore.teacher;

import com.educore.auth.GlobalResponse;
import com.educore.dto.response.StudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/students")
@RequiredArgsConstructor
@Tag(name = "Teacher - Student Management", description = "إدارة الطلاب من قبل المعلم (قبول/رفض/عرض)")
public class TeacherStudentController {

    private final TeacherStudentService teacherStudentService;

    @Operation(summary = "عرض الطلاب المنتظرين", description = "يرجع قائمة ببروفايلات الطلاب الذين لم يتم تفعيل حساباتهم بعد")
    @GetMapping("/pending")
    public ResponseEntity<GlobalResponse<Page<StudentResponse>>> getPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StudentResponse> pendingStudents = teacherStudentService.getPendingStudents(pageable);

        return ResponseEntity.ok(GlobalResponse.<Page<StudentResponse>>builder()
                .success(true)
                .message("تم جلب قائمة الطلاب المنتظرين")
                .data(pendingStudents)
                .build());
    }

    @Operation(summary = "قبول تفعيل حساب طالب", description = "يغير حالة الطالب إلى ACTIVE ويسمح له بالدخول للمنصة")
    @PostMapping("/{id}/approve")
    public ResponseEntity<GlobalResponse<Void>> approve(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GlobalResponse.<Void>builder()
                            .success(false)
                            .error("Unauthorized")
                            .message("يجب تسجيل الدخول كمعلم أولاً")
                            .build());
        }

        teacherStudentService.approveStudent(id, principal.getName());

        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم تفعيل حساب الطالب بنجاح")
                .build());
    }

    @Operation(summary = "رفض حساب طالب", description = "يرفض الحساب مع تسجيل سبب الرفض")
    @PostMapping("/{id}/reject")
    public ResponseEntity<GlobalResponse<Void>> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String reason = request.getOrDefault("reason", "لم يتم ذكر سبب");
        teacherStudentService.rejectStudent(id, reason, principal.getName());

        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم رفض الحساب وإخطار الطالب")
                .build());
    }
}