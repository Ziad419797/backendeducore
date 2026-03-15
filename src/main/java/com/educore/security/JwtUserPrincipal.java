package com.educore.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class JwtUserPrincipal implements UserDetails {

    private final JwtData jwtData;

    public JwtUserPrincipal(JwtData jwtData) {
        this.jwtData = jwtData;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + jwtData.role()));
    }

    @Override
    public String getPassword() {
        return null; // لا نستخدم كلمة المرور في JWT
    }

    @Override
    public String getUsername() {
        return jwtData.phone();
    }

    public Long getUserId() {
        return jwtData.userId();
    }

    public String getRole() {
        return jwtData.role();
    }

    public String getDeviceId() {
        return jwtData.deviceId();
    }

    public String getSessionId() {
        return jwtData.sessionId();
    }

    public String getName() {
        return jwtData.name();
    }

    public String getStudentCode() {
        return jwtData.studentCode();
    }

    public String getStatus() {
        return jwtData.status();
    }

    @Override
    public boolean isAccountNonExpired() {
        return jwtData.isValid();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"REJECTED".equals(jwtData.status()) && !"SUSPENDED".equals(jwtData.status());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return jwtData.isValid();
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(jwtData.status());
    }

    // طريقة مساعدة للتحقق من الدور
    public boolean hasRole(String role) {
        return this.jwtData.role().equals(role);
    }

    // طريقة للحصول على معلومات إضافية
    public Map<String, Object> getAdditionalInfo() {
        return Map.of(
                "phone", jwtData.phone(),
                "userId", jwtData.userId(),
                "role", jwtData.role(),
                "name", jwtData.name(),
                "studentCode", jwtData.studentCode(),
                "status", jwtData.status(),
                "deviceId", jwtData.deviceId(),
                "sessionId", jwtData.sessionId()
        );
    }
}