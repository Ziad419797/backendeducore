package com.educore.security;

import java.time.LocalDateTime;

public record JwtData(
        String phone,           // رقم الهاتف
        String role,            // الدور (STUDENT, PARENT, TEACHER)
        Long userId,            // معرف المستخدم
        String deviceId,        // معرف الجهاز
        String sessionId,       // معرف الجلسة
        String token,           // التوكن نفسه (جديد)

        // ⭐ الحقول الجديدة المطلوبة
        LocalDateTime expiry,   // تاريخ انتهاء الصلاحية
        LocalDateTime issuedAt, // تاريخ الإصدار
        String tokenType,       // نوع التوكن (ACCESS, REFRESH)

        // معلومات إضافية
        String studentCode,     // كود الطالب (اختياري)
        String name,            // الاسم (اختياري)
        String status           // حالة الحساب (ACTIVE, PENDING, etc) - اختياري
) {

    // Constructor مع التوكن
    public JwtData(String phone, String role, Long userId, String deviceId, String sessionId, String token) {
        this(
                phone,
                role,
                userId,
                deviceId,
                sessionId,
                token,
                null,    // expiry
                null,    // issuedAt
                null,    // tokenType
                null,    // studentCode
                null,    // name
                null     // status
        );
    }

    // Constructor مع التوكن والحقول الجديدة
    public JwtData(String phone, String role, Long userId, String deviceId, String sessionId, String token,
                   LocalDateTime expiry, LocalDateTime issuedAt, String tokenType,
                   String studentCode, String name, String status) {
        this.phone = phone;
        this.role = role;
        this.userId = userId;
        this.deviceId = deviceId;
        this.sessionId = sessionId;
        this.token = token;
        this.expiry = expiry;
        this.issuedAt = issuedAt;
        this.tokenType = tokenType;
        this.studentCode = studentCode;
        this.name = name;
        this.status = status;
    }

    // Constructor قديم للحفاظ على التوافق (بدون token)
    public JwtData(String phone, String role, Long userId, String deviceId, String sessionId) {
        this(phone, role, userId, deviceId, sessionId, null);
    }

    // Constructor مع الحقول الجديدة فقط (بدون device و session)
    public JwtData(String phone, String role, Long userId,
                   LocalDateTime expiry, LocalDateTime issuedAt, String tokenType,
                   String studentCode, String name, String status) {
        this(phone, role, userId, null, null, null, expiry, issuedAt, tokenType, studentCode, name, status);
    }

    // طريقة مساعدة للتحقق إذا كان التوكن منتهي الصلاحية
    public boolean isExpired() {
        return expiry != null && expiry.isBefore(LocalDateTime.now());
    }

    // طريقة مساعدة للتحقق إذا كان التوكن صالحاً
    public boolean isValid() {
        return !isExpired();
    }

    // طريقة مساعدة للحصول على الوقت المتبقي
    public long getRemainingSeconds() {
        if (expiry == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), expiry).getSeconds();
    }

    // طريقة مساعدة للحصول على اسم المستخدم
    public String getUserDisplayName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return phone != null ? phone : "مستخدم";
    }

    // طريقة لمعرفة إذا كان مستخدم
    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    public boolean isParent() {
        return "PARENT".equals(role);
    }

    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }

    // طريقة لمعرفة إذا كان الحساب نشط
    public boolean isAccountActive() {
        return "ACTIVE".equals(status);
    }

    // طريقة لمعرفة إذا كان الحساب معلق
    public boolean isAccountPending() {
        return "PENDING".equals(status);
    }

    // طريقة لمعرفة إذا كان الحساب مرفوض
    public boolean isAccountRejected() {
        return "REJECTED".equals(status);
    }

    // طريقة للحصول على معلومات موجزة
    public String getSummary() {
        return String.format("%s (%s) - %s",
                getUserDisplayName(),
                role,
                studentCode != null ? studentCode : phone);
    }

    // Builder pattern للسهولة
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String phone;
        private String role;
        private Long userId;
        private String deviceId;
        private String sessionId;
        private String token;
        private LocalDateTime expiry;
        private LocalDateTime issuedAt;
        private String tokenType;
        private String studentCode;
        private String name;
        private String status;

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder expiry(LocalDateTime expiry) {
            this.expiry = expiry;
            return this;
        }

        public Builder issuedAt(LocalDateTime issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder studentCode(String studentCode) {
            this.studentCode = studentCode;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public JwtData build() {
            return new JwtData(phone, role, userId, deviceId, sessionId, token,
                    expiry, issuedAt, tokenType, studentCode, name, status);
        }
    }
}