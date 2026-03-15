package com.educore.dtocourse.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekCreateRequest {

    @NotBlank
    private String title;

    private String description;
    private Integer orderNumber;
    @NotEmpty(message = "At least one session is required")
    private Set<Long> sessionIds;

//    @NotNull
//    private Long unitId;
}

