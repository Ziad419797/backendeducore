package com.educore.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. تعطيل CSRF لأننا نستخدم JWT
                .csrf(AbstractHttpConfigurer::disable)

                // 2. إعداد CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. سياسة الجلسات (Stateless لـ JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. تكوين الـ Authorization
                .authorizeHttpRequests(auth -> auth
                        // 4.1 الـ endpoints العامة (بدون توكن)
                        .requestMatchers(
                                // Authentication & Registration
                                "/api/auth/**",
                                "/api/student/register/**",
                                "/api/parent/**",
                                // Public endpoints
                                "/api/public/**",
                                "/api/health",
                                "/api/test/**",
                                "/api/categories/**",
                                "/api/courses/**",

                                "/api/sessions/**",
                                "/api/materials/**",
                                "/api/levels/**",
                                "/api/weeks/**",

//                                "/api/quizzes/**",
//                                "/api/questions/**",
//                                "/api/quiz-attempts/**",
                                "/api/assignment-attempts/**",
                                "/api/assignment-questions/**",
                                "/api/assignments/**",



                                // Swagger & API Docs
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",

                                // H2 Console (لتطوير فقط)
                                "/h2-console/**",

                                // Actuator (للمراقبة)
                                "/actuator/**",

                                // Static resources
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",

                                // Root
                                "/"
                        ).permitAll()

                        // 4.2 الـ endpoints الخاصة بالطلاب (تتطلب توكن طالب)
                        // في الـ SecurityConfig.java
                        .requestMatchers("/api/quizzes/**").hasAnyRole("STUDENT", "TEACHER")
                        .requestMatchers("/api/questions/**").hasRole("TEACHER")
                        .requestMatchers("/api/leaderboard/**").hasAnyRole("TEACHER","STUDENT")

                        .requestMatchers("/api/quiz-attempts/**").hasAnyRole("STUDENT", "TEACHER")
                        .requestMatchers(
                                "/api/student/**",
                                "/student/**"
                        ).hasRole("STUDENT")

                        // 4.3 الـ endpoints الخاصة بأولياء الأمور
                        .requestMatchers(
                                "/api/parent/**",
                                "/parent/**"
                        ).hasRole("PARENT")

                        // 4.4 الـ endpoints الخاصة بالمعلمين
                        .requestMatchers(
                                "/api/teacher/**",
                                "/teacher/**"
                        ).hasRole("TEACHER")

                        // 4.5 الـ endpoints الخاصة بالإدارة
                        .requestMatchers(
                                "/api/admin/**",
                                "/admin/**"
                        ).hasRole("ADMIN")

                        // 4.6 أي طلب آخر يتطلب مصادقة
                        .anyRequest().authenticated()
                )

                // 5. إضافة فلتر JWT قبل فلتر المصادقة القياسي
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 6. إعداد استثناءات للأخطاء
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write(
                                    "{\"success\":false,\"error\":\"غير مصرح\",\"message\":\"يرجى تسجيل الدخول أولاً\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().write(
                                    "{\"success\":false,\"error\":\"غير مسموح\",\"message\":\"ليس لديك صلاحية للوصول إلى هذا المورد\"}"
                            );
                        })
                )

                // 7. السماح بإطارات H2 Console (لتطوير فقط)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // السماح بجميع الأصول (في Development فقط)
        config.setAllowedOriginPatterns(List.of("*"));

        // السماح بجميع الطرق
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        // السماح بجميع الـ Headers
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control",
                "X-Device-Id",
                "X-Session-Id",
                "Origin"
        ));

        // كشف الـ Headers
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Device-Id",
                "X-Session-Id"
        ));

        // السماح بـ Credentials
        config.setAllowCredentials(true);

        // Cache لمدة ساعة
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // زيادة القوة إلى 12
    }

    /* =========================
       تكوينات إضافية للأمان
       ========================= */
    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/webjars/**",
                "/error",
                "/favicon.ico"
        );
    }
}