package com.educore.dto.request;

import lombok.Data;

@Data
public class LogoutRequest {
    private String phone;
    private String deviceId;
}