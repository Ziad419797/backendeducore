package com.educore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

// استكمال تسجيل الدخول: Parent يدخل OTP
@Data
public class ParentCompleteLoginRequest {
    @NotBlank(message = "رقم الطالب مطلوب")
    @Pattern(regexp = "^01[0-9]{9}$", message = "رقم الطالب غير صحيح")
    private String ParentPhone;

    private Integer otp;
}
