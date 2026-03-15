package com.educore.dtocourse.response;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private List<CategoryResponse> categories; // تم تغيير units لـ categories
//    private List<UnitSummaryResponse> units;
}

