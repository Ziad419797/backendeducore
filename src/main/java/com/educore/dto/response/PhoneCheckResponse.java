package com.educore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneCheckResponse {
    private boolean exists;
    private String message;
    private String status; // PENDING, ACTIVE, REJECTED
    private String studentCode;
}