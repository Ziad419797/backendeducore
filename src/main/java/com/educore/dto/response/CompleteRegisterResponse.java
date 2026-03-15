package com.educore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Response for completing registration
@Getter
@AllArgsConstructor
public class CompleteRegisterResponse {

    private String message;
    private String phone;
    private String token;
    private String studentCode;
}
