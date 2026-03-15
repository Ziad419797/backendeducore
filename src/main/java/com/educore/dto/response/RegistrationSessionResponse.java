package com.educore.dto.response;

import lombok.Data;

@Data
public class RegistrationSessionResponse {
    private boolean hasActiveSession;
    private String phone;
    private String message;
    private boolean requiresRestart;
}