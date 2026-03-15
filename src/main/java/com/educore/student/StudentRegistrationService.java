package com.educore.student;

import com.educore.dto.mapper.RegistrationMapper;
import com.educore.dto.mapper.StudentMapper;
import com.educore.dto.request.CompleteRegisterRequest;
import com.educore.dto.request.StartRegisterRequest;
import com.educore.dto.response.CompleteRegisterResponse;
import com.educore.dto.response.PhoneCheckResponse;
import com.educore.dto.response.ResendOtpResponse;
import com.educore.dto.response.StartRegisterResponse;
import com.educore.exception.ResourceAlreadyExistsException;
import com.educore.parent.Parent;
import com.educore.parent.ParentRepository;
import com.educore.security.JwtService;
import com.educore.security.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentRegistrationService {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StudentMapper studentMapper;
    private final RegistrationMapper registrationMapper;

    /**
     * Step 1: بدء التسجيل وإرسال OTP
     */
    public StartRegisterResponse startRegistration(StartRegisterRequest request) {
        // التحقق من أن الرقم غير مسجل
        if (studentRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new ResourceAlreadyExistsException("رقم الهاتف مسجل بالفعل");
        }

        // توليد وإرسال OTP
        String otp = otpService.generateAndSendOtp(request.getPhone());
        log.info("Registration started for phone: {}, OTP: {}", request.getPhone(), otp);

        return registrationMapper.toStartRegisterResponse(
                "تم إرسال رمز التحقق إلى رقم الهاتف",
                request.getPhone()
        );
    }

    /**
     * Step 2: إكمال التسجيل بعد التحقق من OTP
     */
    @Transactional
    public CompleteRegisterResponse completeRegistration(
            CompleteRegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        // 1. التحقق من تطابق كلمتي المرور
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("كلمة المرور وتأكيد كلمة المرور غير متطابقين");
        }

        // 2. التحقق من OTP
        otpService.verifyOtp(request.getPhone(), String.valueOf(request.getOtp()));

        // 3. التحقق من أن الرقم غير مسجل
        if (studentRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new ResourceAlreadyExistsException("رقم الهاتف مسجل بالفعل");
        }

        // 4. التحقق من اسم المركز إذا كانت الدراسة في مركز
        if (request.getOnline() != null && !request.getOnline() &&
                (request.getCenterName() == null || request.getCenterName().isBlank())) {
            throw new IllegalArgumentException("اسم المركز مطلوب عند اختيار الدراسة في مركز");
        }

        // 5. إنشاء الطالب باستخدام Mapper
        Student student = studentMapper.toEntity(request);
        student.setPassword(passwordEncoder.encode(request.getPassword()));

        // 6. حفظ الطالب
        Student savedStudent = studentRepository.save(student);

        // 7. إنشاء Parent تلقائياً
        Parent parent = new Parent();
        parent.setPhone(savedStudent.getParentPhone());
        parent.setStudent(savedStudent);
        parentRepository.save(parent);

        log.info("Student registered successfully: {}, Code: {}",
                savedStudent.getPhone(), savedStudent.getStudentCode());
        log.info("Parent auto-created with phone: {}", savedStudent.getParentPhone());

        // 8. إنشاء JWT Token
        String sessionId = UUID.randomUUID().toString();
        String token = jwtService.generateToken(
                savedStudent.getPhone(),
                "STUDENT",
                savedStudent.getId(),
                null,
                sessionId,
                savedStudent.getStudentCode(),
                savedStudent.getShortName(),
                savedStudent.getStatus().name()
        );

        return registrationMapper.toCompleteRegisterResponse(
                "تم التسجيل بنجاح! سيتم مراجعة حسابك من قبل الإدارة",
                savedStudent,
                token
        );
    }

    /**
     * إعادة إرسال OTP
     */
    public ResendOtpResponse resendOtp(String phone) {
        // التحقق من أن الرقم غير مسجل
        if (studentRepository.findByPhone(phone).isPresent()) {
            throw new ResourceAlreadyExistsException("رقم الهاتف مسجل بالفعل");
        }

        // إعادة توليد وإرسال OTP
        String otp = otpService.generateAndSendOtp(phone);
        log.info("OTP resent for phone: {}, OTP: {}", phone, otp);

        return new ResendOtpResponse("تم إعادة إرسال رمز التحقق");
    }

    /**
     * توليد كود طالب فريد
     */
    private String generateUniqueStudentCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            code = StudentCodeGenerator.generate();
            attempts++;

            if (attempts >= maxAttempts) {
                throw new IllegalStateException("فشل في توليد كود طالب فريد");
            }
        } while (studentRepository.existsByStudentCode(code));

        return code;
    }

    /**
     * التحقق من حالة الرقم
     */
    public PhoneCheckResponse checkPhoneStatus(String phone) {
        return studentRepository.findByPhone(phone)
                .map(registrationMapper::toPhoneCheckResponse)
                .orElse(registrationMapper.toPhoneCheckResponse(null));
    }

    /**
     * Getter للـ StudentRepository
     */
    public StudentRepository getStudentRepository() {
        return studentRepository;
    }
}