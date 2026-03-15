package com.educore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParentCompleteLoginResponse {
    private String message;
    private String token; // JWT token
}