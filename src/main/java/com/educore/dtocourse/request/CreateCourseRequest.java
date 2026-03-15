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
public class CreateCourseRequest {

    @NotBlank(message = "Course title is required")
    private String title;

    private String description;

    @NotNull(message = "Order number is required")
    private Integer orderNumber;

    @NotEmpty(message = "At least one category is required")
    private Set<Long> categoryIds;

}
