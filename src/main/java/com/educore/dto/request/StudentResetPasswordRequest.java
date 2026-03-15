package com.educore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResetPasswordRequest {
    @NotBlank
    @Pattern(regexp = "^01[0-9]{9}$", message = "رقم الهاتف غير صحيح")
    private String phone;
    @NotBlank
    @Size(min = 6, message = "كلمة المرور يجب أن تكون 6 أحرف على الأقل")
    private String newPassword;
}
