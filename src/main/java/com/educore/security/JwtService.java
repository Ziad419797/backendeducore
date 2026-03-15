package com.educore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret:your-secret-key-for-jwt-token-generation-change-this-in-production}")
    private String secret;

    @Value("${jwt.expiration:1800}") // 30 دقيقة افتراضياً
    private int expiration;

    @Value("${jwt.refresh-expiration:2592000}") // 30 يوم للتجديد
    private int refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * توليد توكن جديد مع جميع الحقول
     */
    public String generateToken(String phone, String role, Long userId,
                                String deviceId, String sessionId,
                                String studentCode, String name, String status) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        claims.put("deviceId", deviceId);
        claims.put("sessionId", sessionId);
        claims.put("phone", phone);
        claims.put("studentCode", studentCode);
        claims.put("name", name);
        claims.put("status", status);
        claims.put("tokenType", "ACCESS");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusSeconds(expiration);

        claims.put("issuedAt", Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
        claims.put("expiry", Date.from(expiry.atZone(ZoneId.systemDefault()).toInstant()));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(phone)
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expiry.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * توليد توكن (للتوافق مع الكود القديم)
     */
    public String generateToken(String phone, String role, Long userId,
                                String deviceId, String sessionId) {
        return generateToken(phone, role, userId, deviceId, sessionId,
                null, null, null);
    }

    /**
     * توليد توكن (للتوافق مع الكود الأقدم)
     */
    public String generateToken(String phone, String role, Long userId) {
        return generateToken(phone, role, userId, null, null,
                null, null, null);
    }

    /**
     * توليد توكن تجديد
     */
    public String generateRefreshToken(String phone, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        claims.put("phone", phone);
        claims.put("tokenType", "REFRESH");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusSeconds(refreshExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(phone)
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expiry.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * تحليل التوكن مع إرجاع JwtData كامل
     */
    public JwtData parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String phone = claims.getSubject();
            String role = claims.get("role", String.class);
            Long userId = claims.get("userId", Long.class);
            String deviceId = claims.get("deviceId", String.class);
            String sessionId = claims.get("sessionId", String.class);
            String studentCode = claims.get("studentCode", String.class);
            String name = claims.get("name", String.class);
            String status = claims.get("status", String.class);
            String tokenType = claims.get("tokenType", String.class);

            Date expiryDate = claims.getExpiration();
            Date issuedAtDate = claims.getIssuedAt();

            LocalDateTime expiry = expiryDate != null ?
                    LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()) : null;
            LocalDateTime issuedAt = issuedAtDate != null ?
                    LocalDateTime.ofInstant(issuedAtDate.toInstant(), ZoneId.systemDefault()) : null;

            return JwtData.builder()
                    .phone(phone)
                    .role(role)
                    .userId(userId)
                    .deviceId(deviceId)
                    .sessionId(sessionId)
                    .token(token)
                    .expiry(expiry)
                    .issuedAt(issuedAt)
                    .tokenType(tokenType)
                    .studentCode(studentCode)
                    .name(name)
                    .status(status)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * التحقق من صلاحية التوكن
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * التحقق من نوع التوكن
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenType = claims.get("tokenType", String.class);
            return "ACCESS".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * استخراج رقم الهاتف من التوكن
     */
    public String extractPhone(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * استخراج معرف المستخدم من التوكن
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * استخراج الدور من التوكن
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * استخراج معرف الجهاز من التوكن
     */
    public String extractDeviceId(String token) {
        return extractClaim(token, claims -> claims.get("deviceId", String.class));
    }

    /**
     * استخراج معرف الجلسة من التوكن
     */
    public String extractSessionId(String token) {
        return extractClaim(token, claims -> claims.get("sessionId", String.class));
    }

    /**
     * استخراج حالة الحساب من التوكن
     */
    public String extractStatus(String token) {
        return extractClaim(token, claims -> claims.get("status", String.class));
    }

    /**
     * استخراج كود الطالب من التوكن
     */
    public String extractStudentCode(String token) {
        return extractClaim(token, claims -> claims.get("studentCode", String.class));
    }

    /**
     * طريقة مساعدة لاستخراج claims
     */
    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * استخراج جميع claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * الحصول على الوقت المتبقي للتوكن (بالثواني)
     */
    public long getRemainingSeconds(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiry = claims.getExpiration();
            if (expiry == null) return 0;

            long remaining = (expiry.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * تجديد التوكن
     */
    public String refreshToken(String oldToken) {
        JwtData jwtData = parseToken(oldToken);

        // إعادة توليد التوكن مع نفس البيانات
        return generateToken(
                jwtData.phone(),
                jwtData.role(),
                jwtData.userId(),
                jwtData.deviceId(),
                jwtData.sessionId(),
                jwtData.studentCode(),
                jwtData.name(),
                jwtData.status()
        );
    }
}