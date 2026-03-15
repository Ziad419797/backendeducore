package com.educore.hybrid;

import com.educore.auth.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

public class DeviceFingerprintUtil {

    // للاختبار فقط
    private static String testFingerprint = null;

    /**
     * توليد بصمة جهاز فريدة
     */
    public static String generate(HttpServletRequest request) {
        // للاختبار: إذا كان هناك test fingerprint، استخدمه
        if (testFingerprint != null) {
            return testFingerprint;
        }

        // ✅ استخدام أكثر من عامل لتوليد بصمة فريدة
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();  // ⬅️ أضف IP
        String language = request.getHeader("Accept-Language");
        String encoding = request.getHeader("Accept-Encoding");
        String platform = request.getHeader("Sec-Ch-Ua-Platform"); // متصفحات حديثة

        // إذا الـ IP نفس، نحتاج عامل إضافي
        String uniqueFactor = UUID.randomUUID().toString().substring(0, 8);

        // ⚠️ مهم: تضمين الوقت (لكن لا نستخدمه كمؤقت)
        String raw = String.format("UA:%s|IP:%s|LANG:%s|ENC:%s|PLAT:%s|RND:%s",
                userAgent != null ? userAgent : "unknown",
                ip != null ? ip : "0.0.0.0",
                language != null ? language : "unknown",
                encoding != null ? encoding : "unknown",
                platform != null ? platform : "unknown",
                uniqueFactor);

        return HashUtil.sha256(raw);
    }

    /**
     * للاختبار: تعيين بصمة يدوياً
     */
    public static void setTestFingerprint(String fingerprint) {
        testFingerprint = fingerprint;
    }

    /**
     * للاختبار: توليد بصمة مختلفة
     */
    public static String generateDifferent() {
        return HashUtil.sha256(UUID.randomUUID().toString());
    }
}