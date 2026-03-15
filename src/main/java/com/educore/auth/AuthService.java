package com.educore.auth;

import com.educore.dto.mapper.TeacherMapper;
import com.educore.dto.request.TeacherLoginRequest;
import com.educore.dto.response.AuthResponse;
import com.educore.dto.request.LoginRequest;
import com.educore.dto.request.ParentStartLoginRequest;
import com.educore.dto.response.TeacherAuthResponse;
import com.educore.exception.AuthenticationException;
import com.educore.hybrid.DeviceFingerprintUtil;
import com.educore.parent.Parent;
import com.educore.parent.ParentRepository;
import com.educore.security.JwtData;
import com.educore.security.JwtService;
import com.educore.security.OtpService;
import com.educore.session.DatabaseSessionService;
import com.educore.student.Student;
import com.educore.student.StudentRepository;
import com.educore.student.StudentStatus;
import com.educore.teacher.Teacher;
import com.educore.teacher.TeacherRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DatabaseSessionService databaseSessionService;
    private  final TeacherMapper teacherMapper;
    private final OtpService otpService; // تأكدي من عمل Inject لها
    @Value("${app.support.whatsapp:+201234567890}")
    private String supportWhatsApp;

    @Value("${app.session.timeout:30}")
    private int sessionTimeoutMinutes;

    @Value("${app.max.sessions.per.user:3}")
    private int maxSessionsPerUser;

    /* =========================
       ENTRY POINT - نقطة الدخول الرئيسية
       ========================= */
    public Map<String, Object> entryPoint(String token, String deviceId) {
        // 1️⃣ مفيش توكن خالص → صفحة REGISTER
        if (token == null || token.isEmpty()) {
            log.debug("No token provided, showing register page");
            return handleNoToken();
        }

        // 2️⃣ التحقق من صلاحية التوكن في قاعدة البيانات
        if (!databaseSessionService.isTokenValid(token)) {
            log.debug("Token invalid or blacklisted, showing login page");
            return handleInvalidToken("انتهت صلاحية الجلسة. يرجى تسجيل الدخول مرة أخرى");
        }

        try {
            // 3️⃣ تحليل التوكن
            JwtData jwtData = jwtService.parseToken(token);

            // 4️⃣ التحقق من صلاحية الجلسة حسب نوع المستخدم
            if ("STUDENT".equals(jwtData.role())) {
                return handleStudentEntryPoint(jwtData, deviceId);
            } else {
                return handleOtherUserEntryPoint(jwtData, deviceId);
            }

        } catch (Exception e) {
            log.warn("Invalid token at entry point: {}", e.getMessage());
            return handleInvalidToken("توكن غير صالح");
        }
    }

    /* =========================
       STUDENT LOGIN مع Device Lock
       ========================= */
    @Transactional
    public AuthResponse studentLogin(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Student login attempt for phone: {}", request.getPhone());

        Student student = studentRepository
                .findByPhone(request.getPhone())
                .orElseThrow(() ->
                        new AuthenticationException("رقم الهاتف أو كلمة المرور غير صحيحة")
                );

        // 🔴 التحقق من حالة الحساب أولاً
        validateStudentStatus(student);

        // 🔴 التحقق من كلمة المرور
        if (!passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new AuthenticationException("رقم الهاتف أو كلمة المرور غير صحيحة");
        }

        String deviceId = DeviceFingerprintUtil.generate(httpRequest);

        // 🔴 التحقق من عدد الجلسات النشطة
        int activeSessions = databaseSessionService.getActiveSessionsCount(student.getId());
        if (activeSessions >= maxSessionsPerUser) {
            throw new AuthenticationException(
                    "تجاوزت الحد الأقصى للجلسات النشطة. يرجى تسجيل الخروج من جهاز آخر"
            );
        }

        // 🔴 التحقق من صلاحية الجهاز
        if (databaseSessionService.isValidDevice(student.getId(), deviceId)) {
            // نفس الجهاز - تجديد الجلسة
            var existingSession = databaseSessionService.getUserSession(student.getId());

            if (existingSession.isPresent()) {
                String existingToken = (String) existingSession.get().get("token");

                // تحديث النشاط
                databaseSessionService.updateUserActivity(student.getId());

                // تحديث وقت النشاط للطالب
                student.updateActivity();
                studentRepository.save(student);

                log.info("Session renewed for same device: {}", student.getStudentCode());

                return new AuthResponse(
                        existingToken,
                        "تم تجديد الجلسة بنجاح",
                        deviceId,
                        student.getStudentCode(),
                        student.getDevicesCount(),
                        student.getLogoutCount(),
                        student.getStatus().name()
                );
            }
        } else if (activeSessions > 0) {
            // جهاز مختلف مع وجود جلسات نشطة
            throw new AuthenticationException(
                    "الحساب مسجل دخول على جهاز آخر. يجب تسجيل الخروج أولاً"
            );
        }

        // 🟢 إنشاء جلسة جديدة
        String sessionId = UUID.randomUUID().toString();
        String token = jwtService.generateToken(
                student.getPhone(),
                "STUDENT",
                student.getId(),
                deviceId,
                sessionId
        );

        // حفظ الجلسة في قاعدة البيانات
        databaseSessionService.saveSession(
                student.getId(),
                "STUDENT",
                token,
                deviceId,
                sessionId,
                sessionTimeoutMinutes
        );

        // تحديث حالة الطالب
        student.activateDevice(deviceId, sessionId);
        student.setLastLoginAt(LocalDateTime.now());
        studentRepository.save(student);

        log.info("New session created for student: {}", student.getStudentCode());

        return new AuthResponse(
                token,
                "تم تسجيل الدخول بنجاح",
                deviceId,
                student.getStudentCode(),
                student.getDevicesCount(),
                student.getLogoutCount(),
                student.getStatus().name()
        );
    }

    /* =========================
       STUDENT LOGOUT (Manual)
       ========================= */
    @Transactional
    public void studentLogout(String token) {
        log.info("Student logout requested");

        try {
            // 1. تحليل التوكن
            JwtData data = jwtService.parseToken(token);

            // 2. التحقق من role
            if (!"STUDENT".equals(data.role())) {
                throw new AuthenticationException("غير مصرح لتسجيل الخروج. Role: " + data.role());
            }

            // 3. حذف الجلسة من قاعدة البيانات
            databaseSessionService.deleteUserSession(data.userId(), token);
            // 2️⃣ تفريغ الجهاز النشط من الطالب ❗❗
            studentRepository.findById(data.userId()).ifPresent(student -> {
                student.clearActiveSession(); // ⬅️ مهمة جدًا
                studentRepository.save(student);
            });
            log.info("Student logout successful for user: {}", data.userId());

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage(), e);
            throw new RuntimeException("فشل تسجيل الخروج: " + e.getMessage(), e);
        }
    }

    /* =========================
       forget password process for students
       ========================= */


    // 1. بدء عملية استعادة كلمة المرور
    public void initiateStudentForgotPassword(String phone) {
        studentRepository.findByPhone(phone)
                .orElseThrow(() -> new AuthenticationException("رقم الهاتف غير مسجل كطالب لدينا"));

        otpService.generateAndSendOtp(phone);
        log.info("Forget password process started for student: {}", phone);
    }

    // 2. إعادة إرسال الكود (Resend)
    public void resendStudentOtp(String phone) {
        studentRepository.findByPhone(phone)
                .orElseThrow(() -> new AuthenticationException("المستخدم غير موجود"));

        otpService.generateAndSendOtp(phone);
        log.info("OTP resent to student: {}", phone);
    }

    // 3. التحقق من الكود
    public void verifyStudentOtp(String phone, String otp) {
        // الـ otpService هيرمي Exception لو الكود غلط والـ GlobalHandler هيهندله
        otpService.verifyOtp(phone, otp);
        log.info("OTP verified successfully for student: {}", phone);
    }

    // 4. تغيير كلمة المرور النهائية
    @Transactional
    public void resetStudentPassword(String phone, String newPassword) {
        Student student = studentRepository.findByPhone(phone)
                .orElseThrow(() -> new AuthenticationException("فشل العثور على بيانات الطالب"));

        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);

        log.info("Password reset successful for student code: {}", student.getStudentCode());
    }














    /* =========================
       PARENT LOGIN
       ========================= */
    @Transactional
    public AuthResponse parentLogin(ParentStartLoginRequest request) {
        log.info("Parent login attempt for phone: {}", request.getParentPhone());

        Parent parent = parentRepository
                .findByPhone(request.getParentPhone())
                .orElseThrow(() ->
                        new AuthenticationException("رقم الهاتف غير مسجل")
                );

        String sessionId = UUID.randomUUID().toString();
        String token = jwtService.generateToken(
                parent.getPhone(),
                "PARENT",
                parent.getId(),
                null,
                sessionId
        );

        // حفظ الجلسة
        databaseSessionService.saveSession(
                parent.getId(),
                "PARENT",
                token,
                "WEB",
                sessionId,
                sessionTimeoutMinutes
        );

        return new AuthResponse(token, "تم تسجيل الدخول بنجاح", null, null, null, null, "ACTIVE");
    }

    /* =========================
       TEACHER LOGIN
       ========================= */
    @Transactional
    public TeacherAuthResponse teacherLogin(TeacherLoginRequest request) {
        log.info("Teacher login attempt for phone: {}", request.getPhone());

        // 1. البحث عن المدرس
        Teacher teacher = teacherRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new AuthenticationException("بيانات الدخول غير صحيحة"));


        // 2. التأكد من كلمة المرور
        if (!passwordEncoder.matches(request.getPassword(), teacher.getPassword())) {
            throw new AuthenticationException("بيانات الدخول غير صحيحة");
        }

        // 3. التأكد من أن الحساب مفعل
        if (!teacher.isEnabled()) {
            throw new AuthenticationException("حساب المعلم غير مفعل، يرجى التواصل مع الإدارة");
        }

        // 4. توليد الجلسة والتوكنز
        String sessionId = UUID.randomUUID().toString();

        // Access Token (30 min)
        String accessToken = jwtService.generateToken(
                teacher.getPhone(), "TEACHER", teacher.getId(), null, sessionId
        );

        // Refresh Token (30 days)
        String refreshToken = jwtService.generateRefreshToken(
                teacher.getPhone(), "TEACHER", teacher.getId()
        );

        // 5. حفظ الجلسة في الداتابيز
        databaseSessionService.saveSession(
                teacher.getId(), "TEACHER", accessToken, "WEB", sessionId, sessionTimeoutMinutes
        );

        // 6. استخدام المابر لتحويل النتائج
        TeacherAuthResponse response = teacherMapper.toAuthResponse(teacher);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setMessage("تم تسجيل دخول المعلم بنجاح");


        return response;

    }

    /* =========================
       HELPERS - دوال مساعدة
       ========================= */

    // التحقق من حالة الطالب
    private void validateStudentStatus(Student student) {
        if (!student.isEnabled()) {
            if (student.getStatus() == StudentStatus.PENDING) {
                throw new AuthenticationException(
                        "حسابك قيد المراجعة من قبل الإدارة. " +
                                "لأي استفسار، يرجى التواصل مع الدعم الفني عبر واتساب: " + supportWhatsApp
                );
            } else if (student.getStatus() == StudentStatus.REJECTED) {
                throw new AuthenticationException("الحساب مرفوض. يرجى التواصل مع الإدارة");
            } else {
                throw new AuthenticationException("حساب الطالب غير مفعل");
            }
        }

        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new AuthenticationException("حالة الحساب غير صالحة للتسجيل");
        }
    }

    // التحقق من كلمة المرور
    private void validatePassword(String raw, String encoded) {
        if (!passwordEncoder.matches(raw, encoded)) {
            throw new AuthenticationException("رقم الهاتف أو كلمة المرور غير صحيحة");
        }
    }

    /* =========================
       ENTRY POINT HANDLERS - معالجات نقطة الدخول
       ========================= */

    // 1️⃣ حالة: لا يوجد توكن → صفحة التسجيل
    private Map<String, Object> handleNoToken() {
        return Map.of(
                "screen", "REGISTER",
                "title", "إنشاء حساب جديد",
                "message", "سجل الآن للانضمام لمنصتنا التعليمية",
                "showLoginLink", true,
                "loginLinkText", "لو عندك اكونت دوس هنا",
                "loginUrl", "/login"
        );
    }

    // 2️⃣ حالة: توكن غير صالح → صفحة تسجيل الدخول
    private Map<String, Object> handleInvalidToken(String reason) {
        return Map.of(
                "screen", "LOGIN",
                "title", "تسجيل الدخول",
                "message", reason != null ? reason : "يرجى تسجيل الدخول",
                "showRegisterLink", true,
                "registerLinkText", "لو انت طالب جديد دوس هنا",
                "registerUrl", "/register"
        );
    }

    // 3️⃣ حالة: حساب PENDING → صفحة الانتظار
    private Map<String, Object> handlePendingStudent(Student student) {
        // تحديث نشاط المستخدم في قاعدة البيانات
        databaseSessionService.updateUserActivity(student.getId());

        // تحديث وقت النشاط للطالب
        student.updateActivity();
        studentRepository.save(student);

        return Map.of(
                "screen", "PENDING",
                "title", "حسابك قيد المراجعة",
                "message", "الاكونت لسه بيتراجع من قبل الإدارة",
                "subMessage", "لأي استفسار كلم الدعم الفني",
                "supportWhatsApp", supportWhatsApp,
                "whatsappLink", "https://wa.me/" + supportWhatsApp.replace("+", ""),
                "whatsappButtonText", "تواصل مع الدعم عبر واتساب",
                "studentCode", student.getStudentCode(),
                "phone", student.getPhone(),
                "showLogoutButton", true
        );
    }

    // 4️⃣ حالة: حساب ACTIVE → الصفحة الرئيسية
    private Map<String, Object> handleActiveStudent(Student student, JwtData jwtData) {
        // تحديث نشاط المستخدم في قاعدة البيانات
        databaseSessionService.updateUserActivity(student.getId());

        // تحديث وقت النشاط للطالب
        student.updateActivity();
        studentRepository.save(student);

        // إنشاء JWT جديد (Refresh)
        String newToken = jwtService.generateToken(
                student.getPhone(),
                "STUDENT",
                student.getId(),
                jwtData.deviceId(),
                jwtData.sessionId()
        );

        // تجديد مدة الجلسة في قاعدة البيانات
        databaseSessionService.extendSession(jwtData.token(), sessionTimeoutMinutes);

        return Map.of(
                "screen", "HOME",
                "title", "مرحباً بعودتك",
                "welcomeMessage", "أهلاً " + student.getShortName(),
                "studentCode", student.getStudentCode(),
                "fullName", student.getFullName(),
                "redirectUrl", "/student/home",
                "jwt", newToken,
                "autoRedirect", true,
                "autoRedirectDelay", 500
        );
    }

    // 5️⃣ حالة: حساب REJECTED → صفحة الرفض
    private Map<String, Object> handleRejectedStudent(Student student) {
        return Map.of(
                "screen", "REJECTED",
                "title", "الحساب مرفوض",
                "message", "للأسف، تم رفض حسابك من قبل الإدارة",
                "rejectionReason", student.getRejectionReason() != null ?
                        student.getRejectionReason() : "لم يتم تحديد السبب",
                "supportWhatsApp", supportWhatsApp,
                "whatsappLink", "https://wa.me/" + supportWhatsApp.replace("+", ""),
                "contactAdminMessage", "للاستفسار، يرجى التواصل مع الإدارة",
                "showContactButton", true
        );
    }

    // 6️⃣ حالة: جهاز مختلف → صفحة تسجيل الدخول
    private Map<String, Object> handleDifferentDevice() {
        return Map.of(
                "screen", "LOGIN",
                "title", "تم تسجيل الدخول من جهاز آخر",
                "message", "الحساب مسجل دخول على جهاز آخر. يجب تسجيل الخروج من الجهاز الآخر أولاً",
                "showRegisterLink", false,
                "isDeviceConflict", true
        );
    }

    /* =========================
       معالجة نقطة دخول الطالب
       ========================= */
    private Map<String, Object> handleStudentEntryPoint(JwtData jwtData, String deviceId) {
        String tokenDeviceId = jwtData.deviceId(); // ✔️ المصدر الصح

        try {
            Student student = studentRepository
                    .findById(jwtData.userId())
                    .orElseThrow(() -> new RuntimeException("الطالب غير موجود"));

            // 🔴 التحقق من صلاحية الجهاز في قاعدة البيانات
            if (!databaseSessionService.isValidDevice(jwtData.userId(), tokenDeviceId)) {
                log.info("Device mismatch for student: {}", student.getStudentCode());
                return handleDifferentDevice();
            }

            // 🔴 التحقق من انتهاء الجلسة
            if (databaseSessionService.isSessionExpired(student.getId())) {
                return handleInvalidToken("انتهت صلاحية الجلسة");
            }

            // 🔴 التحقق من حالة الحساب
            switch (student.getStatus()) {
                case PENDING:
                    return handlePendingStudent(student);

                case ACTIVE:
                    if (!student.isEnabled()) {
                        return handlePendingStudent(student);
                    }
                    return handleActiveStudent(student, jwtData);

                case REJECTED:
                    return handleRejectedStudent(student);

                default:
                    return handleInvalidToken("حالة حساب غير معروفة");
            }

        } catch (Exception e) {
            log.error("Error handling student entry point: {}", e.getMessage());
            return handleInvalidToken("حدث خطأ في النظام");
        }
    }

    /* =========================
       معالجة نقطة دخول المستخدمين الآخرين
       ========================= */
    private Map<String, Object> handleOtherUserEntryPoint(JwtData jwtData, String deviceId) {
        // التحقق من صلاحية الجلسة
        if (!databaseSessionService.isTokenValid(jwtData.token())) {
            return handleInvalidToken("انتهت صلاحية الجلسة");
        }

        // تحديث النشاط
        databaseSessionService.updateUserActivity(jwtData.userId());

        String redirectPath = "/" + jwtData.role().toLowerCase() + "/home";

        return Map.of(
                "screen", "HOME",
                "title", "مرحباً بعودتك",
                "role", jwtData.role(),
                "redirectUrl", redirectPath,
                "autoRedirect", true,
                "autoRedirectDelay", 300
        );
    }

    /* =========================
       دالة مساعدة جديدة: تسجيل الخروج القسري من كل الجلسات
       ========================= */
    @Transactional
    public void forceLogoutAllSessions(Long userId, String userType) {
        log.info("Force logging out all sessions for user: {}, type: {}", userId, userType);

        // تحديث حالة الطالب إذا كان student
        if ("STUDENT".equals(userType)) {
            studentRepository.findById(userId).ifPresent(student -> {
                student.clearActiveSession();
                studentRepository.save(student);
            });
        }

        // حذف جميع الجلسات من قاعدة البيانات
        databaseSessionService.forceLogoutAll(userId, userType);
    }

    /* =========================
       GETTERS
       ========================= */
    public JwtService getJwtService() {
        return jwtService;
    }

    public DatabaseSessionService getDatabaseSessionService() {
        return databaseSessionService;
    }

    public StudentRepository getStudentRepository() {
        return studentRepository;
    }
}