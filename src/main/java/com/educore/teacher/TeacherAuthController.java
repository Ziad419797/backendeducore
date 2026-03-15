package com.educore.teacher;

import com.educore.auth.GlobalResponse;
import com.educore.dto.request.ForgotPasswordRequest;
import com.educore.dto.request.ResetPasswordRequest;
import com.educore.dto.request.VerifyOtpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth/teacher")
@RequiredArgsConstructor
@Tag(name = "Teacher Authentication", description = "Endpoints لإدارة استعادة كلمة مرور المعلم")
public class TeacherAuthController {

    private final TeacherAuthService teacherAuthService;

    @Operation(summary = "طلب استعادة كلمة المرور", description = "يرسل كود OTP لرقم هاتف المعلم المسجل")
    @PostMapping("/forgot-password")
    public ResponseEntity<GlobalResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        teacherAuthService.initiateForgotPassword(request.getPhone());

        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم إرسال كود التحقق إلى هاتفك")
                .build());
    }

    @Operation(summary = "التحقق من كود الـ OTP", description = "يتحقق من صحة الكود المرسل للمستخدم")
    @PostMapping("/verify-otp")
    public ResponseEntity<GlobalResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        // تعديل: تحويل الـ Integer إلى String لو الـ Service محتاجة String
        // ومناداة الميثود مباشرة لأن الـ Validation بيعتمد على الـ Exception
        teacherAuthService.verifyOtp(request.getPhone(), String.valueOf(request.getOtp()));

        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("الكود صحيح، يمكنك تغيير كلمة المرور")
                .build());
    }

    @Operation(summary = "تعيين كلمة مرور جديدة", description = "يقوم بتحديث كلمة المرور في قاعدة البيانات")
    @PostMapping("/reset-password")
    public ResponseEntity<GlobalResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        teacherAuthService.resetPassword(request.getPhone(), request.getNewPassword());

        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم تغيير كلمة المرور بنجاح، يمكنك تسجيل الدخول الآن")
                .build());
    }
}