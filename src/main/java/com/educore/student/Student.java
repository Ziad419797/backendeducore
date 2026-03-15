package com.educore.student;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_student_phone", columnList = "phone"),
        @Index(name = "idx_student_code", columnList = "studentCode"),
        @Index(name = "idx_student_status", columnList = "status")
})public class Student {

    // ==================== Primary Key ====================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== Authentication ====================
    @Column(unique = true, nullable = false, length = 11)
    private String phone;

    @Column(nullable = false, length = 11)
    private String parentPhone;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = false;

    // ==================== Personal Information ====================
    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(length = 50)
    private String secondName;

    @Column(length = 50)
    private String thirdName;

    @Column(length = 50)
    private String fourthName;

    // ==================== Academic Information ====================
    @Column(nullable = false, length = 50)
    private String grade;

    @Column(nullable = false, length = 50)
    private String governorate;

    @Column(nullable = false, length = 100)
    private String area;

    @Column(nullable = false, length = 150)
    private String schoolName;

    @Column(length = 100)
    private String educationDepartment;

    // ==================== Study Type ====================
    @Column(nullable = false)
    private Boolean online;

    @Column(length = 100)
    private String centerName;

    // ==================== Documents ====================
    @Column(length = 500)
    private String profileImageUrl;

    @Column(length = 500)
    private String identityDocumentUrl;

    // ==================== System Fields ====================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status = StudentStatus.PENDING;

    @Column(length = 5, unique = true)
    private String studentCode;

    // ==================== Device & Session Management ====================
    @Column(name = "active_device_id")
    private String activeDeviceId;

    @Column(name = "active_session_id")
    private String activeSessionId;

    @Column(name = "last_device_fingerprint")
    private String lastDeviceFingerprint;

    @Column(name = "devices_count")
    private Integer devicesCount = 0;

    @Column(name = "logout_count")
    private Integer logoutCount = 0;

    @Column(name = "login_count")
    private Integer loginCount = 0;

    // ==================== Timestamps ====================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_logout_at")
    private LocalDateTime lastLogoutAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // ==================== Auditing Fields ====================
    @Column(name = "created_by", length = 50)
    private String createdBy = "SYSTEM";

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "rejected_by", length = 50)
    private String rejectedBy;

    // ==================== PrePersist & PreUpdate ====================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // توليد كود الطالب إذا لم يكن موجوداً
        if (this.studentCode == null || this.studentCode.isBlank()) {
            this.studentCode = StudentCodeGenerator.generate();
        }

        // تعيين قيم افتراضية
        if (this.devicesCount == null) this.devicesCount = 0;
        if (this.logoutCount == null) this.logoutCount = 0;
        if (this.loginCount == null) this.loginCount = 0;
        if (this.enabled == false) this.enabled = (this.status == StudentStatus.ACTIVE);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        // تحديث الحالة بناءً على التواريخ
        if (this.status == StudentStatus.ACTIVE && this.approvedAt == null) {
            this.approvedAt = LocalDateTime.now();
            this.enabled = true;
        } else if (this.status == StudentStatus.REJECTED && this.rejectedAt == null) {
            this.rejectedAt = LocalDateTime.now();
            this.enabled = false;
        } else if (this.status == StudentStatus.PENDING) {
            this.enabled = false;
        }
    }

    // ==================== Helper Methods ====================
    public boolean hasActiveSession() {
        return activeDeviceId != null && activeSessionId != null && activeDeviceId.trim().length() > 0;
    }

    public boolean isSameDevice(String deviceId) {
        return deviceId != null && deviceId.equals(activeDeviceId);
    }

    public boolean isDeviceAllowed(String deviceId) {
        // يمكنك إضافة منطق أكثر تعقيداً هنا
        return !hasActiveSession() || isSameDevice(deviceId);
    }

    public void activateDevice(String deviceId, String sessionId) {
        this.activeDeviceId = deviceId;
        this.activeSessionId = sessionId;
        this.lastDeviceFingerprint = deviceId;
        this.lastLoginAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();

        if (this.devicesCount == null) {
            this.devicesCount = 1;
        } else {
            this.devicesCount++;
        }

        if (this.loginCount == null) {
            this.loginCount = 1;
        } else {
            this.loginCount++;
        }
    }


    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public void approve(String approvedBy) {
        this.status = StudentStatus.ACTIVE;
        this.enabled = true;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approvedBy;
    }

    public void reject(String rejectedBy, String reason) {
        this.status = StudentStatus.REJECTED;
        this.enabled = false;
        this.rejectedAt = LocalDateTime.now();
        this.rejectedBy = rejectedBy;
        this.rejectionReason = reason;
    }


    public void activate(String updatedBy) {
        this.status = StudentStatus.ACTIVE;
        this.enabled = true;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Display Methods ====================
    public String getFullName() {
        StringBuilder fullName = new StringBuilder(firstName);

        if (secondName != null && !secondName.isBlank()) {
            fullName.append(" ").append(secondName);
        }
        if (thirdName != null && !thirdName.isBlank()) {
            fullName.append(" ").append(thirdName);
        }
        if (fourthName != null && !fourthName.isBlank()) {
            fullName.append(" ").append(fourthName);
        }

        return fullName.toString();
    }

    public String getShortName() {
        return firstName + (secondName != null && !secondName.isBlank() ? " " + secondName : "");
    }

    public String getStudyType() {
        return online ? "أونلاين" : "حضور في مركز";
    }

    public String getStudyLocation() {
        if (online) {
            return "دراسة أونلاين";
        } else {
            return centerName != null ? "مركز " + centerName : "مركز غير محدد";
        }
    }

    // ==================== Status Methods ====================
    public boolean isPending() {
        return status == StudentStatus.PENDING;
    }

    public boolean isActive() {
        return status == StudentStatus.ACTIVE && enabled;
    }

    public boolean isRejected() {
        return status == StudentStatus.REJECTED;
    }


    public boolean canLogin() {
        return isActive() && !hasActiveSession();
    }

    // ==================== Utility Methods ====================
    public long getSessionDurationMinutes() {
        if (lastLoginAt == null) return 0;

        LocalDateTime endTime = hasActiveSession() ? LocalDateTime.now() : lastLogoutAt;
        if (endTime == null) return 0;

        return java.time.Duration.between(lastLoginAt, endTime).toMinutes();
    }

    public boolean isSessionExpired(int timeoutMinutes) {
        if (lastActivityAt == null || !hasActiveSession()) return false;

        long inactiveMinutes = java.time.Duration.between(lastActivityAt, LocalDateTime.now()).toMinutes();
        return inactiveMinutes > timeoutMinutes;
    }


    // في نهاية class Student.java إضافة هذه الميثودات:


    public boolean isSessionValid(int timeoutMinutes) {
        if (lastActivityAt == null || !hasActiveSession()) {
            return false;
        }

        long inactiveMinutes = java.time.Duration.between(lastActivityAt, LocalDateTime.now()).toMinutes();
        return inactiveMinutes <= timeoutMinutes;
    }

    /**
     * تجديد الجلسة
     */
    public void renewSession() {
        this.lastActivityAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void clearActiveSession() {
        this.activeDeviceId = null;
        this.activeSessionId = null;
        this.lastDeviceFingerprint = null;
        this.lastLogoutAt = LocalDateTime.now();

        this.logoutCount = (this.logoutCount == null) ? 1 : this.logoutCount + 1;
    }



    // ==================== toString for Debugging ====================
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", studentCode='" + studentCode + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", status=" + status +
                ", enabled=" + enabled +
                ", hasActiveSession=" + hasActiveSession() +
                '}';
    }
}