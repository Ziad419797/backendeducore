package com.educore.dtocourse.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private Long id;

    private String title;

    private String description;

    private Integer orderNumber;

    private Boolean active;

    private Set<Long> courseIds; // Sessions belong to multiple courses
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
