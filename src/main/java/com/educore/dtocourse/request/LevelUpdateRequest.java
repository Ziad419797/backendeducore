package com.educore.dtocourse.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelUpdateRequest {

    @NotBlank
    private String name;
}
