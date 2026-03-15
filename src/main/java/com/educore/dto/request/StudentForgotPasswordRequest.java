package com.educore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentForgotPasswordRequest {
    @NotBlank
    private String phone; // 👈 ضيفي private هنا}
}
