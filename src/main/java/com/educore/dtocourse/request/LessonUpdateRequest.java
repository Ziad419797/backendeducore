package com.educore.dtocourse.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonUpdateRequest {

    @NotBlank
    private String title;
    private String description;
    private Integer orderNumber;
    private Boolean active;
}
