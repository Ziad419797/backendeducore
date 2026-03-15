package com.educore.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;         // JWT Token
    private String message;       // رسالة النجاح
    private String deviceId;      // بصمة الجهاز
    private String studentCode;   // كود الطالب
    private Integer devicesCount; // عدد الأجهزة
    private Integer logoutCount;  // عدد مرات Logout
    private String accountStatus;  // ✅ أضف هذا الحقل

}
