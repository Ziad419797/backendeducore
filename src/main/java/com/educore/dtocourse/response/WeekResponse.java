package com.educore.dtocourse.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekResponse {

    private Long id;
    private String title;
    private String description;
    private Integer orderNumber;
    private boolean active;
    private Set<Long> sessionIds; // الأسبوع قد يتكرر في أكثر من ساشن
//    private Long unitId;
//    private String unitTitle;
    private LocalDateTime createdAt;
    private Set<LessonMaterialResponse> materials;
}

