package com.educore.dtocourse.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelCreateRequest {

    @NotBlank
    private String name; // أولى ثانوي
}

