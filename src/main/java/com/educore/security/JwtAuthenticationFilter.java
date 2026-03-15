package com.educore.security;

import com.educore.session.DatabaseSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final DatabaseSessionService databaseSessionService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();
        log.debug("🔐 JWT Filter - {} {}", method, path);

        // استثناء endpoints العامة
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            unauthorized(response, "مطلوب توكن للوصول إلى هذا المورد");
            return;
        }

        String token = header.substring(7);

        try {
            // 1. التحقق من صلاحية التوكن في قاعدة البيانات
            if (!databaseSessionService.isTokenValid(token)) {
                log.warn("Invalid or expired token for path: {}", path);
                unauthorized(response, "التوكن منتهي الصلاحية أو غير صالح");
                return;
            }

            // 2. تحليل التوكن
            JwtData jwtData = jwtService.parseToken(token);

            // 3. التحقق الخاص بالطلاب
            if ("STUDENT".equals(jwtData.role())) {
                if (!validateStudentSession(jwtData, request)) {
                    log.warn("Invalid student session for user: {}", jwtData.userId());
                    unauthorized(response, "جلسة غير صالحة أو الجهاز غير مصرح");
                    return;
                }
            }

            // 4. تحديث نشاط المستخدم في قاعدة البيانات
            databaseSessionService.updateUserActivity(jwtData.userId());

            // 5. إنشاء Principal باستخدام JwtData الكامل
            JwtUserPrincipal principal = new JwtUserPrincipal(jwtData);

            // 6. حفظ في SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("✅ Authenticated user: {} with role: {}",
                    jwtData.phone(), jwtData.role());

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("❌ JWT Authentication error for path {}: {}", path, e.getMessage(), e);
            unauthorized(response, "فشل في التحقق من الهوية");
        }
    }

    /* =========================
       التحقق من جلسة الطالب
       ========================= */
    private boolean validateStudentSession(JwtData jwtData, HttpServletRequest request) {
        try {
            String deviceId = extractDeviceIdFromRequest(request);

            if (deviceId == null) {
                log.warn("Device ID not found in request for user: {}", jwtData.userId());
                return false;
            }

            // 1. التحقق من وجود الجلسة في قاعدة البيانات
            Optional<Map<String, Object>> sessionOpt = databaseSessionService.getUserSession(jwtData.userId());
            if (sessionOpt.isEmpty()) {
                log.warn("No active session found in database for user: {}", jwtData.userId());
                return false;
            }

            Map<String, Object> session = sessionOpt.get();

            // 2. التحقق من الجهاز
            String storedDeviceId = (String) session.get("deviceId");
            if (storedDeviceId == null || !storedDeviceId.equals(deviceId)) {
                log.warn("Device mismatch for user: {}. Expected: {}, Got: {}",
                        jwtData.userId(), storedDeviceId, deviceId);
                return false;
            }

            // 3. التحقق من انتهاء الجلسة
            if (databaseSessionService.isSessionExpired(jwtData.userId())) {
                log.warn("Session expired for user: {}", jwtData.userId());
                return false;
            }

            // 4. التحقق من صلاحية التوكن المخزن
            String storedToken = (String) session.get("token");
            if (storedToken == null || !storedToken.equals(jwtData.token())) {
                log.warn("Token mismatch for user: {}", jwtData.userId());
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating student session for user {}: {}",
                    jwtData.userId(), e.getMessage());
            return false;
        }
    }

    /* =========================
       استخراج Device ID من الطلب
       ========================= */
    private String extractDeviceIdFromRequest(HttpServletRequest request) {
        // تحقق من الـ Header أولاً
        String deviceHeader = request.getHeader("X-Device-Id");
        if (deviceHeader != null && !deviceHeader.trim().isEmpty()) {
            return deviceHeader;
        }

        // تحقق من الـ Parameter
        String deviceParam = request.getParameter("deviceId");
        if (deviceParam != null && !deviceParam.trim().isEmpty()) {
            return deviceParam;
        }

        // إنشاء Device ID من معلومات الطلب (كبديل)
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        if (userAgent != null && ipAddress != null) {
            return String.format("%s-%s", ipAddress, userAgent.hashCode());
        }

        return null;
    }

    /* =========================
       التحقق من Endpoints العامة
       ========================= */
    private boolean isPublicEndpoint(String path) {
        // قائمة بجميع الـ endpoints العامة
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/student/register/") ||
                path.startsWith("/api/parent/") ||

                path.startsWith("/api/sessions/") ||
                path.startsWith("/api/materials/") ||
                path.startsWith("/api/levels/") ||
                path.startsWith("/api/weeks/") ||
                path.startsWith("/api/categories") ||
                path.startsWith("/api/courses") ||
//                path.startsWith("/api/questions") ||
//
//                path.startsWith("/api/quizzes") ||
//                path.startsWith("/api/quiz-attempts") ||
                path.startsWith("/api/assignment-attempts") ||
                path.startsWith("/api/assignment-questions") ||
                path.startsWith("/api/assignments") ||



                path.startsWith("/api/public/") ||
                path.startsWith("/api/test/") ||
                path.startsWith("/error") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.equals("/favicon.ico") ||
                path.equals("/") ||
                // إضافة endpoints جديدة حسب الحاجة
                path.startsWith("/api/health") ||
                path.startsWith("/actuator") ||
                path.startsWith("/h2-console") ||
                // الـ static resources
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/webjars/");
    }

    /* =========================
       رد Unauthorized مع تفاصيل أكثر
       ========================= */
    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        log.debug("🚫 Unauthorized access: {}", message);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"success\":false,\"error\":\"غير مصرح\",\"message\":\"%s\",\"redirectTo\":\"/login\"}",
                message
        );

        response.getWriter().write(jsonResponse);
    }

    /* =========================
       تجاوز الفلتر لبعض الطلبات
       ========================= */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // عدم فلترة الطلبات OPTIONS (لـ CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // عدم فلترة الـ static resources
        if (path.startsWith("/static/") ||
                // Public APIs
                path.startsWith("/api/auth") ||
                path.startsWith("/api/public") ||
                path.startsWith("/api/levels") ||
                path.startsWith("/api/categories") ||
                path.startsWith("/api/sessions") ||

                path.startsWith("/api/weeks") ||
                path.startsWith("/api/courses") ||

//                path.startsWith("/api/questions") ||
//
//                path.startsWith("/api/quizzes") ||
//                path.startsWith("/api/quiz-attempts") ||

                path.startsWith("/api/materials") ||
                path.startsWith("/api/assignment-attempts") ||
                path.startsWith("/api/assignment-questions") ||
                path.startsWith("/api/assignments") ||

                path.startsWith("/api/health") ||

                // H2 & Actuator
                path.startsWith("/h2-console") ||
                path.startsWith("/actuator") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".ico")) {
            return true;
        }

        return false;
    }
}