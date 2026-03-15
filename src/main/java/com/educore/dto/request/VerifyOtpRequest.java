package com.educore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "رقم الهاتف مطلوب")
    @Pattern(regexp = "^01[0-9]{9}$", message = "رقم الهاتف غير صحيح")
    private String phone;

    @NotNull(message = "رمز التحقق مطلوب")
    private Integer otp;
}