package com.educore.parent;

import com.educore.dto.request.ParentCompleteLoginRequest;
import com.educore.dto.request.ParentStartLoginRequest;
import com.educore.dto.response.ParentCompleteLoginResponse;
import com.educore.dto.response.ParentStartLoginResponse;
import com.educore.security.JwtService;
import com.educore.security.OtpService;
import com.educore.student.Student;
import com.educore.student.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ParentLoginService {

    private final StudentRepository studentRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    public ParentLoginService(StudentRepository studentRepository, OtpService otpService, JwtService jwtService) {
        this.studentRepository = studentRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    // 1️⃣ إرسال OTP لولي الأمر باستخدام رقم ولي الأمر
    public ParentStartLoginResponse startLogin(ParentStartLoginRequest request) {
        log.info("بدء تسجيل الدخول لولي الأمر برقم: {}", request.getParentPhone());

        // البحث عن الطالب اللي ولي أمره هذا الرقم
        Student student = studentRepository.findByParentPhone(request.getParentPhone())
                .orElseThrow(() -> {
                    log.warn("لم يتم العثور على طالب مرتبط برقم ولي الأمر: {}", request.getParentPhone());
                    return new IllegalArgumentException("رقم ولي الأمر غير مسجل");
                });

        // إرسال OTP على رقم ولي الأمر
        String otp = otpService.generateAndSendOtp(student.getParentPhone());
        log.info("تم إرسال OTP لولي الأمر {}: {}", student.getParentPhone(), otp);

        return new ParentStartLoginResponse("تم إرسال رمز التحقق إلى رقم ولي الأمر");
    }

    // 2️⃣ التحقق من OTP وعمل login
    @Transactional
    public ParentCompleteLoginResponse completeLogin(ParentCompleteLoginRequest request) {
        log.info("محاولة تسجيل الدخول لولي الأمر برقم: {}", request.getParentPhone());

        Student student = studentRepository.findByParentPhone(request.getParentPhone())
                .orElseThrow(() -> {
                    log.warn("لم يتم العثور على طالب مرتبط برقم ولي الأمر: {}", request.getParentPhone());
                    return new IllegalArgumentException("رقم ولي الأمر غير مسجل");
                });

        otpService.verifyOtp(student.getParentPhone(), String.valueOf(request.getOtp()));
        log.info("تم التحقق من OTP بنجاح لولي الأمر: {}", student.getParentPhone());

        String token = jwtService.generateToken(student.getParentPhone(), "PARENT", student.getId());
        log.info("تم إنشاء JWT Token لولي الأمر: {}", student.getParentPhone());

        return new ParentCompleteLoginResponse("تم تسجيل الدخول بنجاح", token);
    }
}
