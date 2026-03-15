package com.educore.session;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_token", columnList = "token", unique = true),
        @Index(name = "idx_expires_at", columnList = "expiresAt"),
        @Index(name = "idx_user_device", columnList = "userId, deviceId")
})
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String userType; // STUDENT, PARENT, TEACHER

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false, length = 100)
    private String deviceId;

    @Column(length = 50)
    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(nullable = false)
    private boolean blacklisted = false;

    @Column(length = 100)
    private String blacklistReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    // Constructor
    public UserSession() {}

    public UserSession(Long userId, String userType, String token,
                       String deviceId, String sessionId,
                       LocalDateTime expiresAt) {
        this.userId = userId;
        this.userType = userType;
        this.token = token;
        this.deviceId = deviceId;
        this.sessionId = sessionId;
        this.expiresAt = expiresAt;
        this.lastActivityAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isExpired() && !blacklisted;
    }

    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public void blacklist(String reason) {
        this.blacklisted = true;
        this.blacklistReason = reason;
    }

    public boolean isSameDevice(String deviceId) {
        return this.deviceId.equals(deviceId);
    }
}