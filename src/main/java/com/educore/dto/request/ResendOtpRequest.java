package com.educore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

// Request for resending OTP
@Getter
@AllArgsConstructor
public class ResendOtpRequest {
    @NotBlank(message = "رقم الهاتف مطلوب")
    @Pattern(regexp = "^01[0-9]{9}$", message = "رقم الهاتف غير صحيح")
    private String phone;
}