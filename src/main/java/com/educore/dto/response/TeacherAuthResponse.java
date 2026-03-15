package com.educore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherAuthResponse {
    private String accessToken;
    private String refreshToken;
    private String name;
    private String phone;
    private String role; // دايما TEACHER
    private String message;
}