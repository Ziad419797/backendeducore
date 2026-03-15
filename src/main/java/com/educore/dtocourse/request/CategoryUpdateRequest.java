package com.educore.dtocourse.request;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryUpdateRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    private Boolean active;

}
