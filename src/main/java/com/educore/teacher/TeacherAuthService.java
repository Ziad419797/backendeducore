package com.educore.teacher;

import com.educore.exception.AuthenticationException;
import com.educore.security.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAuthService {

    private final TeacherRepository teacherRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    // المرحلة 1: إرسال الـ OTP
    public void initiateForgotPassword(String phone) {
        // التأكد أولاً أن المدرس موجود
        teacherRepository.findByPhone(phone)
                .orElseThrow(() -> new AuthenticationException("رقم الهاتف غير مسجل لدينا"));

        // توليد وإرسال OTP (الخدمة دي غالباً بتسيف الكود في Redis أو DB داخلياً)
        otpService.generateAndSendOtp(phone);

        log.info("Forget password initiated for phone: {}", phone);
    }

    // المرحلة 2: التحقق من الـ OTP
    public void verifyOtp(String phone, String otp) {
        // نكتفي بمناداة ميثود الـ verify
        // لو الكود غلط، الـ otpService غالباً بيرمي Exception وإحنا بنهندله في الـ GlobalExceptionHandler
        otpService.verifyOtp(phone, otp);

        log.info("OTP verified successfully for phone: {}", phone);
    }

    // المرحلة 3: تعيين الباسورد الجديد
    @Transactional
    public void resetPassword(String phone, String newPassword) {
        Teacher teacher = teacherRepository.findByPhone(phone)
                .orElseThrow(() -> new AuthenticationException("حدث خطأ، المستخدم غير موجود"));

        // تشفير الباسورد الجديد وحفظه
        teacher.setPassword(passwordEncoder.encode(newPassword));
        teacherRepository.save(teacher);

        log.info("Password has been reset successfully for teacher: {}", phone);
    }
}