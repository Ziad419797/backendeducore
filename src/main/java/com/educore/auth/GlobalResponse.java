package com.educore.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    public static <T> GlobalResponse<T> success(T data, String message) {
        return GlobalResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> GlobalResponse<T> success(T data) {
        return success(data, "تمت العملية بنجاح");
    }
}