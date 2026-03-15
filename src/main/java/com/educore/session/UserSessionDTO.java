package com.educore.session;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionDTO {
    private String id;
    private Long userId;
    private String userType;
    private String deviceId;
    private String sessionId;
    private LocalDateTime expiresAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime createdAt;
    private boolean blacklisted;
    private String blacklistReason;
    private boolean isValid;

    public static UserSessionDTO fromEntity(UserSession session) {
        return UserSessionDTO.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .userType(session.getUserType())
                .deviceId(session.getDeviceId())
                .sessionId(session.getSessionId())
                .expiresAt(session.getExpiresAt())
                .lastActivityAt(session.getLastActivityAt())
                .createdAt(session.getCreatedAt())
                .blacklisted(session.isBlacklisted())
                .blacklistReason(session.getBlacklistReason())
                .isValid(session.isValid())
                .build();
    }
}