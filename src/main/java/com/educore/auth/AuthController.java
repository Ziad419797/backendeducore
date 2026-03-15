package com.educore.auth;

import com.educore.dto.request.*;
import com.educore.dto.response.AuthResponse;
import com.educore.dto.response.TeacherAuthResponse;
import com.educore.exception.AuthenticationException;
import com.educore.hybrid.DeviceFingerprintUtil;
import com.educore.security.JwtData;
import com.educore.security.JwtUserPrincipal;
import com.educore.student.Student;
import com.educore.student.StudentRepository;
import com.educore.student.StudentStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final StudentRepository studentRepository;
    private final AuthService authService;

    /* =========================
       نقطة الدخول الرئيسية
       ========================= */
    @GetMapping("/entry-point")
    public ResponseEntity<?> entryPoint(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            log.debug("Entry point check called");

            String token = extractToken(authHeader);
            String deviceId = DeviceFingerprintUtil.generate(request);

            Map<String, Object> response = authService.entryPoint(token, deviceId);

            log.debug("Entry point response: {}", response.get("screen"));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Entry point error: {}", e.getMessage(), e);

            return ResponseEntity.ok(Map.of(
                    "screen", "LOGIN",
                    "title", "حدث خطأ",
                    "message", "حدث خطأ في النظام، يرجى المحاولة مرة أخرى",
                    "showRegisterLink", true
            ));
        }
    }

    /* =========================
       Student Login
       ========================= */
    @PostMapping("/student/login")
    public ResponseEntity<?> studentLogin(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            log.info("Student login attempt for phone: {}", request.getPhone());

            AuthResponse authResponse = authService.studentLogin(request, httpRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", authResponse.getToken());
            response.put("message", authResponse.getMessage());
            response.put("accountStatus", authResponse.getAccountStatus());
            response.put("studentCode", authResponse.getStudentCode());
            response.put("deviceId", authResponse.getDeviceId());
            response.put("devicesCount", authResponse.getDevicesCount());
            response.put("logoutCount", authResponse.getLogoutCount());

            Student student = studentRepository.findByPhone(request.getPhone()).orElse(null);

            if (student != null) {
                response.put("accountStatus", student.getStatus().name());

                if (student.getStatus() == StudentStatus.PENDING) {
                    response.put("redirectTo", "/pending");
                    response.put("supportWhatsApp", "+201234567890");
                } else if (student.getStatus() == StudentStatus.ACTIVE) {
                    response.put("redirectTo", "/student/home");
                    response.put("autoRedirect", true);
                }

                log.info("Student login successful: {}", student.getStudentCode());
            } else {
                response.put("accountStatus", "UNKNOWN");
                log.info("Student login successful but student not found in DB");
            }

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            log.warn("Student login failed: {}", e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("screen", "LOGIN");

            // إضافة معلومات إضافية لـ PENDING accounts
            if (e.getMessage().contains("قيد المراجعة")) {
                Student student = studentRepository.findByPhone(request.getPhone()).orElse(null);
                if (student != null && student.getStatus() == StudentStatus.PENDING) {
                    error.put("redirectTo", "/pending");
                    error.put("status", "PENDING");
                    error.put("supportWhatsApp", "+201234567890");
                }
            }

            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Unexpected error during student login: {}", e.getMessage(), e);

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "حدث خطأ غير متوقع أثناء تسجيل الدخول",
                    "screen", "LOGIN"
            ));
        }
    }

    /* =========================
       Student Logout
       ========================= */
    @PostMapping("/student/logout")
    public ResponseEntity<?> studentLogout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            log.info("Student logout requested");

            String token = extractToken(authHeader);

            if (token != null) {
                try {
                    authService.studentLogout(token);
                } catch (Exception e) {
                    log.warn("Service logout failed, continuing: {}", e.getMessage());
                }
            }

            log.info("Student logout successful");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "تم تسجيل الخروج بنجاح",
                    "redirectTo", "/login"
            ));

        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage(), e);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "تم تسجيل الخروج",
                    "redirectTo", "/login"
            ));
        }
    }


    /* =========================
    forgot password flow for students
       ========================= */


    @Operation(summary = "طلب استعادة كلمة المرور", description = "يرسل كود OTP لرقم الطالب")
    @PostMapping("/forgot-password")
    public ResponseEntity<GlobalResponse<Void>> forgotPassword(@Valid @RequestBody StudentForgotPasswordRequest request) {
        authService.initiateStudentForgotPassword(request.getPhone());
        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم إرسال كود التحقق بنجاح")
                .build());
    }

    @Operation(summary = "إعادة إرسال كود التحقق", description = "يرسل كود جديد في حالة عدم وصول القديم")
    @PostMapping("/resend-otp")
    public ResponseEntity<GlobalResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendStudentOtp(request.getPhone());
        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم إعادة إرسال الكود")
                .build());
    }

    @Operation(summary = "التأكد من كود التحقق", description = "يتحقق من صحة الـ OTP المبعوث")
    @PostMapping("/verify-otp")
    public ResponseEntity<GlobalResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyStudentOtp(request.getPhone(), String.valueOf(request.getOtp()));
        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("كود التحقق صحيح")
                .build());
    }

    @Operation(summary = "تعيين كلمة المرور الجديدة", description = "تحديث الباسورد في قاعدة البيانات")
    @PostMapping("/reset-password")
    public ResponseEntity<GlobalResponse<Void>> resetPassword(@Valid @RequestBody StudentResetPasswordRequest request) {
        authService.resetStudentPassword(request.getPhone(), request.getNewPassword());
        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم تغيير كلمة المرور بنجاح، يمكنك تسجيل الدخول الآن")
                .build());
    }

    /* =========================
       Parent Login
       ========================= */
    @PostMapping("/parent/login")
    public ResponseEntity<?> parentLogin(@Valid @RequestBody ParentStartLoginRequest request) {
        try {
            AuthResponse authResponse = authService.parentLogin(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", authResponse.getToken());
            response.put("message", authResponse.getMessage());
            response.put("redirectTo", "/parent/home");
            response.put("autoRedirect", true);

            log.info("Parent login successful for phone: {}", request.getParentPhone());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            log.warn("Parent login failed: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "screen", "LOGIN"
            ));
        }
    }

    /* =========================
       Teacher Login
       ========================= */
    @PostMapping("/teacher/login")
    public ResponseEntity<GlobalResponse<TeacherAuthResponse>> teacherLogin(
            @Valid @RequestBody TeacherLoginRequest request // استخدمنا الـ DTO الجديد
    ) {
        try {
            // 1. نداء السيرفيس اللي بترجع الـ Response الكامل (مع الـ Refresh Token)
            TeacherAuthResponse authResponse = authService.teacherLogin(request);

            log.info("Teacher login successful for phone: {}", request.getPhone());

            // 2. استخدام ApiResponse موحد (Standard Response)
            return ResponseEntity.ok(GlobalResponse.<TeacherAuthResponse>builder()
                    .success(true)
                    .message(authResponse.getMessage())
                    .data(authResponse) // الداتا هنا فيها الـ tokens والاسم والـ role
                    .build());

        } catch (AuthenticationException e) {
            log.warn("Teacher login failed: {}", e.getMessage());

            // 3. إرجاع خطأ منظم للفرونت إيند
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GlobalResponse.<TeacherAuthResponse>builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }
    /* =========================
       التحقق من حالة الجلسة
       ========================= */
    @GetMapping("/session-status")
    public ResponseEntity<?> getSessionStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Map<String, Object> response = new HashMap<>();

        String token = extractToken(authHeader);

        if (token != null) {
            try {
                JwtData jwtData = authService.getJwtService().parseToken(token);

                response.put("hasSession", true);
                response.put("role", jwtData.role());
                response.put("message", "يوجد جلسة نشطة");

                if ("STUDENT".equals(jwtData.role())) {
                    Student student = studentRepository.findById(jwtData.userId()).orElse(null);
                    if (student != null) {
                        response.put("status", student.getStatus().name());
                        response.put("studentCode", student.getStudentCode());

                        if (student.getStatus() == StudentStatus.PENDING) {
                            response.put("redirectTo", "/pending");
                        } else if (student.getStatus() == StudentStatus.ACTIVE) {
                            response.put("redirectTo", "/student/home");
                        }
                    }
                }
            } catch (Exception e) {
                // التوكن غير صالح
                log.debug("Invalid token in session check: {}", e.getMessage());
                response.put("hasSession", false);
                response.put("message", "انتهت الجلسة");
                response.put("redirectTo", "/login");
            }
        } else {
            response.put("hasSession", false);
            response.put("message", "لا توجد جلسة نشطة");
            response.put("redirectTo", "/register");
            response.put("showLoginLink", true);
        }

        return ResponseEntity.ok(response);
    }

    /* =========================
       Pending Status - حالة الحساب المعلق
       ========================= */
    @GetMapping("/pending-status")
    public ResponseEntity<?> getPendingStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Map<String, Object> response = new HashMap<>();

        String token = extractToken(authHeader);

        if (token == null) {
            response.put("redirectTo", "/login");
            return ResponseEntity.ok(response);
        }

        try {
            JwtData jwtData = authService.getJwtService().parseToken(token);

            if (!"STUDENT".equals(jwtData.role())) {
                response.put("redirectTo", "/login");
                return ResponseEntity.ok(response);
            }

            Student student = studentRepository.findById(jwtData.userId()).orElse(null);

            if (student != null && student.getStatus() == StudentStatus.PENDING) {
                response.put("status", "PENDING");
                response.put("message", "حسابك قيد المراجعة من قبل الإدارة");
                response.put("supportWhatsApp", "+201234567890");
                response.put("studentCode", student.getStudentCode());
                response.put("phone", student.getPhone());
                response.put("whatsappLink", "https://wa.me/201234567890");
            } else {
                response.put("redirectTo", "/login");
            }

        } catch (Exception e) {
            log.error("Error getting pending status: {}", e.getMessage());
            response.put("redirectTo", "/login");
        }

        return ResponseEntity.ok(response);
    }

    /* =========================
       Logout العام
       ========================= */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String token = extractToken(authHeader);

        if (token != null) {
            try {
                authService.studentLogout(token);
            } catch (Exception e) {
                log.debug("Logout service error: {}", e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "تم تسجيل الخروج بنجاح");
        response.put("redirectTo", "/login");
        response.put("success", true);

        log.info("General logout successful");
        return ResponseEntity.ok(response);
    }

    /* =========================
       تسجيل الخروج القسري من كل الجلسات
       ========================= */
    @PostMapping("/force-logout-all")
    public ResponseEntity<?> forceLogoutAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            String token = extractToken(authHeader);

            if (token == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "التوكن مطلوب"
                ));
            }

            JwtData jwtData = authService.getJwtService().parseToken(token);

            // تسجيل الخروج من جميع الجلسات
            authService.forceLogoutAllSessions(jwtData.userId(), jwtData.role());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "تم تسجيل الخروج من جميع الأجهزة بنجاح",
                    "redirectTo", "/login"
            ));

        } catch (Exception e) {
            log.error("Force logout error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "فشل تسجيل الخروج من جميع الأجهزة"
            ));
        }
    }

    /* =========================
       التحقق من رقم الهاتف
       ========================= */
    @GetMapping("/check-phone/{phone}")
    public ResponseEntity<?> checkPhone(@PathVariable String phone) {
        try {
            boolean exists = studentRepository.findByPhone(phone).isPresent();

            if (exists) {
                return ResponseEntity.ok(Map.of(
                        "exists", true,
                        "message", "الرقم مسجل بالفعل",
                        "action", "LOGIN",
                        "loginUrl", "/login?phone=" + phone
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "exists", false,
                        "message", "يمكنك التسجيل بهذا الرقم",
                        "action", "REGISTER",
                        "registerUrl", "/register?phone=" + phone
                ));
            }

        } catch (Exception e) {
            log.error("Phone check error: {}", e.getMessage());

            return ResponseEntity.ok(Map.of(
                    "exists", false,
                    "message", "حدث خطأ في التحقق"
            ));
        }
    }

    /* =========================
       اختبار الأجهزة المتعددة (للاختبار فقط)
       ========================= */
    @PostMapping("/student/test-multi-device")
    public ResponseEntity<?> testMultiDevice(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            // محاكاة جهازين مختلفين
            String userAgent1 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
            String userAgent2 = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15";

            Map<String, Object> result = new HashMap<>();

            Student student = studentRepository.findByPhone(request.getPhone())
                    .orElseThrow(() -> new AuthenticationException("الطالب غير موجود"));

            // Device 1 Fingerprint
            String originalUserAgent = httpRequest.getHeader("User-Agent");

            // محاكاة Device 1
            httpRequest.setAttribute("User-Agent", userAgent1);
            String device1Id = DeviceFingerprintUtil.generate(httpRequest);

            // محاكاة Device 2
            httpRequest.setAttribute("User-Agent", userAgent2);
            String device2Id = DeviceFingerprintUtil.generate(httpRequest);

            // استعادة الـ User-Agent الأصلي
            httpRequest.setAttribute("User-Agent", originalUserAgent);

            result.put("device1_fingerprint", device1Id.substring(0, Math.min(20, device1Id.length())) + "...");
            result.put("device2_fingerprint", device2Id.substring(0, Math.min(20, device2Id.length())) + "...");
            result.put("same_device", device1Id.equals(device2Id));
            result.put("active_device_id", student.getActiveDeviceId());
            result.put("has_active_session", student.hasActiveSession());
            result.put("student_code", student.getStudentCode());
            result.put("student_status", student.getStatus().name());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Multi-device test error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /* =========================
       جلب معلومات المستخدم الحالي
       ========================= */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal JwtUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "غير مصرح",
                    "message", "يرجى تسجيل الدخول أولاً"
            ));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("phone", principal.getUsername());
        response.put("role", principal.getRole());
        response.put("userId", principal.getUserId());

        if ("STUDENT".equals(principal.getRole())) {
            Student student = studentRepository.findById(principal.getUserId()).orElse(null);
            if (student != null) {
                response.put("studentCode", student.getStudentCode());
                response.put("fullName", student.getFullName());
                response.put("status", student.getStatus().name());
                response.put("enabled", student.isEnabled());
            }
        }

        return ResponseEntity.ok(response);
    }

    /* =========================
       تجديد الجلسة
       ========================= */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            String token = extractToken(authHeader);

            if (token == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Token مطلوب"
                ));
            }

            JwtData jwtData = authService.getJwtService().parseToken(token);

            // إنشاء توكن جديد
            String newToken = authService.getJwtService().generateToken(
                    jwtData.phone(),
                    jwtData.role(),
                    jwtData.userId(),
                    jwtData.deviceId(),
                    jwtData.sessionId()
            );

            // تجديد الجلسة في قاعدة البيانات
            authService.getDatabaseSessionService().extendSession(token, 30);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", newToken,
                    "message", "تم تجديد الجلسة بنجاح"
            ));

        } catch (Exception e) {
            log.error("Session refresh error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "فشل تجديد الجلسة",
                    "message", e.getMessage()
            ));
        }
    }

    /* =========================
       استخراج التوكن من الـ Header
       ========================= */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}