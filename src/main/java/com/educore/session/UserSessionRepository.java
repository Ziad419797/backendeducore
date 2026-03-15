package com.educore.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    Optional<UserSession> findByToken(String token);

    List<UserSession> findByUserId(Long userId);

    Optional<UserSession> findByUserIdAndDeviceId(Long userId, String deviceId);

    List<UserSession> findByUserIdAndUserType(Long userId, String userType);

    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.userType = :userType AND s.blacklisted = false AND s.expiresAt > :now")
    Optional<UserSession> findActiveSession(@Param("userId") Long userId,
                                            @Param("userType") String userType,
                                            @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.userId = :userId AND s.blacklisted = false AND s.expiresAt > :now")
    int countActiveSessions(@Param("userId") Long userId,
                            @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession s SET s.blacklisted = true, s.blacklistReason = :reason WHERE s.token = :token")
    void blacklistToken(@Param("token") String token,
                        @Param("reason") String reason);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.userId = :userId AND s.userType = :userType")
    void deleteAllUserSessions(@Param("userId") Long userId,
                               @Param("userType") String userType);

    @Modifying
    @Query("UPDATE UserSession s SET s.lastActivityAt = :now WHERE s.token = :token")
    void updateActivity(@Param("token") String token,
                        @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession s SET s.expiresAt = :newExpiry WHERE s.token = :token")
    void extendSession(@Param("token") String token,
                       @Param("newExpiry") LocalDateTime newExpiry);

    boolean existsByTokenAndBlacklistedFalseAndExpiresAtAfter(String token, LocalDateTime now);

    // ⭐⭐ استعلامات جديدة مطلوبة
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.blacklisted = false AND s.expiresAt > :now")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Long userId,
                                                 @Param("now") LocalDateTime now);

    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.deviceId = :deviceId AND s.blacklisted = false AND s.expiresAt > :now")
    Optional<UserSession> findActiveSessionByUserAndDevice(@Param("userId") Long userId,
                                                           @Param("deviceId") String deviceId,
                                                           @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.blacklisted = false AND s.expiresAt > :now")
    long countAllActiveSessions(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(DISTINCT s.userId) FROM UserSession s WHERE s.blacklisted = false AND s.expiresAt > :now")
    long countActiveUsers(@Param("now") LocalDateTime now);
}