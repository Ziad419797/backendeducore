package com.educore.dtocourse.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private Long levelId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
