package com.educore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherLoginRequest {
    @NotBlank(message = "رقم الهاتف مطلوب")
    private String phone;

    @NotBlank(message = "كلمة المرور مطلوبة")
    private String password;
}