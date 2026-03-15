package com.educore.session;

import com.educore.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseSessionService {

    private final UserSessionRepository sessionRepository;
    private final StudentRepository studentRepository;

    /**
     * حفظ جلسة جديدة
     */
    @Transactional
    public UserSession saveSession(Long userId, String userType, String token,
                                   String deviceId, String sessionId,
                                   int timeoutMinutes) {

        // حذف أي جلسات منتهية لهذا المستخدم
        cleanupExpiredSessions(userId);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(timeoutMinutes);

        UserSession session = new UserSession(userId, userType, token,
                deviceId, sessionId, expiresAt);

        return sessionRepository.save(session);
    }

    /**
     * التحقق من صلاحية التوكن
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        Optional<UserSession> sessionOpt = sessionRepository.findByToken(token);

        if (sessionOpt.isEmpty()) {
            return false;
        }

        UserSession session = sessionOpt.get();

        // التحقق من صلاحية الجلسة
        if (session.isExpired()) {
            log.debug("Token expired: {}", token.substring(0, Math.min(20, token.length())));
            return false;
        }

        if (session.isBlacklisted()) {
            log.debug("Token blacklisted: {}", token.substring(0, Math.min(20, token.length())));
            return false;
        }

        // تحديث وقت النشاط
        session.updateActivity();
        sessionRepository.save(session);

        return true;
    }

    /**
     * التحقق من صلاحية الجهاز
     */
    public boolean isValidDevice(Long userId, String deviceId) {
        if (userId == null || deviceId == null) {
            return false;
        }

        List<UserSession> sessions = sessionRepository.findByUserId(userId);

        return sessions.stream()
                .filter(session -> !session.isExpired() && !session.isBlacklisted())
                .anyMatch(session -> session.isSameDevice(deviceId));
    }

    /**
     * التحقق من انتهاء الجلسة
     */
    public boolean isSessionExpired(Long userId) {
        List<UserSession> sessions = sessionRepository.findByUserId(userId);

        return sessions.stream()
                .noneMatch(session -> !session.isExpired() && !session.isBlacklisted());
    }

    /**
     * الحصول على جلسة المستخدم
     */
    public Optional<Map<String, Object>> getUserSession(Long userId) {
        List<UserSession> sessions = sessionRepository.findByUserId(userId);

        // البحث عن أول جلسة نشطة
        Optional<UserSession> activeSession = sessions.stream()
                .filter(session -> !session.isExpired() && !session.isBlacklisted())
                .findFirst();

        if (activeSession.isEmpty()) {
            return Optional.empty();
        }

        UserSession session = activeSession.get();

        Map<String, Object> sessionData = Map.of(
                "userId", session.getUserId(),
                "userType", session.getUserType(),
                "token", session.getToken(),
                "deviceId", session.getDeviceId(),
                "sessionId", session.getSessionId(),
                "expiresAt", session.getExpiresAt(),
                "lastActivityAt", session.getLastActivityAt()
        );

        return Optional.of(sessionData);
    }

    /**
     * تحديث نشاط المستخدم
     */
    @Transactional
    public void updateUserActivity(Long userId) {
        List<UserSession> sessions = sessionRepository.findByUserId(userId);

        sessions.stream()
                .filter(session -> !session.isExpired() && !session.isBlacklisted())
                .forEach(session -> {
                    session.updateActivity();
                    sessionRepository.save(session);
                });
    }

    /**
     * تسجيل الخروج (حذف الجلسة)
     */
    @Transactional
    public void deleteUserSession(Long userId, String token) {
        // Blacklist the token
        sessionRepository.findByToken(token).ifPresent(session -> {
            session.blacklist("User logged out");
            sessionRepository.save(session);
        });

        // Update student status
        studentRepository.findById(userId).ifPresent(student -> {
            student.clearActiveSession();
            studentRepository.save(student);
        });
    }

    /**
     * ⭐⭐ الميثود الناقصة: تسجيل الخروج من جميع الجلسات
     */
    @Transactional
    public void forceLogoutAll(Long userId, String userType) {
        log.info("Force logging out all sessions for user: {}, type: {}", userId, userType);

        // Blacklist all active sessions for this user
        List<UserSession> sessions = sessionRepository.findByUserId(userId);

        sessions.stream()
                .filter(session -> !session.isExpired() && !session.isBlacklisted())
                .forEach(session -> {
                    session.blacklist("Force logout all sessions");
                    sessionRepository.save(session);
                });

        // If it's a student, update the student record
        if ("STUDENT".equals(userType)) {
            studentRepository.findById(userId).ifPresent(student -> {
                student.clearActiveSession();
                studentRepository.save(student);
            });
        }

        log.info("Force logout completed for user: {}, {} sessions blacklisted",
                userId, sessions.size());
    }

    /**
     * ⭐⭐ الميثود الناقصة: حذف جميع جلسات المستخدم
     */
    @Transactional
    public void deleteAllUserSessions(Long userId, String userType) {
        sessionRepository.deleteAllUserSessions(userId, userType);
        log.info("Deleted all sessions for user: {}, type: {}", userId, userType);
    }

    /**
     * ⭐⭐ الميثود الناقصة: جلب جميع الجلسات النشطة للمستخدم
     */
    public List<Map<String, Object>> getUserActiveSessions(Long userId) {
        List<UserSession> sessions = sessionRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (UserSession session : sessions) {
            if (!session.isExpired() && !session.isBlacklisted()) {
                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("id", session.getId());
                sessionInfo.put("deviceId", session.getDeviceId());
                sessionInfo.put("lastActivityAt", session.getLastActivityAt());
                sessionInfo.put("expiresAt", session.getExpiresAt());
                sessionInfo.put("userType", session.getUserType());
                sessionInfo.put("createdAt", session.getCreatedAt());
                sessionInfo.put("token", session.getToken().substring(0, Math.min(20, session.getToken().length())) + "...");
                sessionInfo.put("isValid", session.isValid());
                sessionInfo.put("isExpired", session.isExpired());
                sessionInfo.put("isBlacklisted", session.isBlacklisted());

                result.add(sessionInfo);
            }
        }

        return result;
    }

    /**
     * ⭐⭐ الميثود الناقصة: التحقق من صلاحية الجلسة في Redis
     */
    public boolean isSessionValid(Long userId, String token) {
        return isTokenValid(token) && sessionRepository.findByToken(token)
                .map(session -> session.getUserId().equals(userId))
                .orElse(false);
    }

    /**
     * إضافة التوكن للقائمة السوداء
     */
    @Transactional
    public void blacklistToken(String token, String reason) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            session.blacklist(reason);
            sessionRepository.save(session);
        });
    }

    /**
     * التحقق من وجود التوكن في القائمة السوداء
     */
    public boolean isTokenBlacklisted(String token) {
        return sessionRepository.findByToken(token)
                .map(UserSession::isBlacklisted)
                .orElse(false);
    }

    /**
     * عدد الجلسات النشطة للمستخدم
     */
    public int getActiveSessionsCount(Long userId) {
        return sessionRepository.countActiveSessions(userId, LocalDateTime.now());
    }

    /**
     * ⭐⭐ الميثود الناقصة: التحقق من وجود جلسة نشطة للمستخدم
     */
    public boolean hasActiveSession(Long userId) {
        return getActiveSessionsCount(userId) > 0;
    }

    /**
     * تنظيف الجلسات المنتهية
     */
    @Transactional
    @Scheduled(fixedRate = 300000) // كل 5 دقائق
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.deleteExpiredSessions(now);
        log.info("Expired sessions cleaned up at {}", now);
    }

    /**
     * تنظيف الجلسات المنتهية لمستخدم محدد
     */
    @Transactional
    public void cleanupExpiredSessions(Long userId) {
        List<UserSession> sessions = sessionRepository.findByUserId(userId);

        sessions.stream()
                .filter(UserSession::isExpired)
                .forEach(session -> {
                    session.blacklist("Session expired");
                    sessionRepository.save(session);
                });
    }

    /**
     * تجديد الجلسة
     */
    @Transactional
    public void extendSession(String token, int additionalMinutes) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            if (!session.isExpired() && !session.isBlacklisted()) {
                LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(additionalMinutes);
                session.setExpiresAt(newExpiry);
                sessionRepository.save(session);
                log.debug("Session extended for token: {}", token.substring(0, Math.min(20, token.length())));
            }
        });
    }

    /**
     * ⭐⭐ الميثود الناقصة: جلب معلومات الجلسة بواسطة التوكن
     */
    public Optional<UserSession> getSessionByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    /**
     * ⭐⭐ الميثود الناقصة: جلب جلسات المستخدم حسب النوع
     */
    public List<UserSession> getUserSessionsByType(Long userId, String userType) {
        return sessionRepository.findByUserIdAndUserType(userId, userType);
    }

    /**
     * ⭐⭐ الميثود الناقصة: تجديد جميع الجلسات النشطة للمستخدم
     */
    @Transactional
    public void extendAllActiveSessions(Long userId, int additionalMinutes) {
        List<UserSession> sessions = sessionRepository.findByUserId(userId);

        sessions.stream()
                .filter(session -> !session.isExpired() && !session.isBlacklisted())
                .forEach(session -> {
                    LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(additionalMinutes);
                    session.setExpiresAt(newExpiry);
                    sessionRepository.save(session);
                });

        log.debug("Extended all active sessions for user: {}", userId);
    }

    /**
     * ⭐⭐ الميثود الناقصة: التحقق من وجود جلسة للجهاز
     */
    public Optional<UserSession> getSessionByUserAndDevice(Long userId, String deviceId) {
        return sessionRepository.findByUserIdAndDeviceId(userId, deviceId);
    }

    /**
     * ⭐⭐ الميثود الناقصة: تحويل التوكن إلى قائمة سوداء إذا كان منتهياً
     */
    @Transactional
    public void blacklistIfExpired(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            if (session.isExpired() && !session.isBlacklisted()) {
                session.blacklist("Token expired");
                sessionRepository.save(session);
                log.debug("Blacklisted expired token: {}", token.substring(0, Math.min(20, token.length())));
            }
        });
    }

    /**
     * ⭐⭐ الميثود الناقصة: حذف الجلسات القديمة (أقدم من أيام محددة)
     */
    @Transactional
    public void deleteOldSessions(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        List<UserSession> oldSessions = sessionRepository.findAll().stream()
                .filter(session -> session.getCreatedAt().isBefore(cutoff))
                .collect(Collectors.toList());

        sessionRepository.deleteAll(oldSessions);
        log.info("Deleted {} old sessions (older than {} days)", oldSessions.size(), daysOld);
    }

    /**
     * ⭐⭐ الميثود الناقصة: إحصائيات الجلسات
     */
    public Map<String, Object> getSessionStats() {
        List<UserSession> allSessions = sessionRepository.findAll();

        long totalSessions = allSessions.size();
        long activeSessions = allSessions.stream()
                .filter(session -> !session.isExpired() && !session.isBlacklisted())
                .count();
        long expiredSessions = allSessions.stream()
                .filter(UserSession::isExpired)
                .count();
        long blacklistedSessions = allSessions.stream()
                .filter(UserSession::isBlacklisted)
                .count();

        return Map.of(
                "totalSessions", totalSessions,
                "activeSessions", activeSessions,
                "expiredSessions", expiredSessions,
                "blacklistedSessions", blacklistedSessions,
                "uniqueUsers", allSessions.stream()
                        .map(UserSession::getUserId)
                        .distinct()
                        .count()
        );
    }
}