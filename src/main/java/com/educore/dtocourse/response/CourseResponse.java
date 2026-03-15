package com.educore.dtocourse.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Long id;

    private String title;

    private String description;

    private Integer orderNumber;

    private Boolean active;

    // لإظهار الكاتيجوريز المرتبط بها الكورس
    private Set<Long> categoryIds;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
