package com.educore.dtocourse.response;


import com.educore.lessonmaterial.MaterialType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonMaterialResponse {

    private Long id;
    private MaterialType materialType;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Set<Long> weekIds; // تكرار الماتريال في أكثر من أسبوع
    private Integer downloadCount;
    private Boolean preview;
    private Long durationSeconds;
    private LocalDateTime createdAt;
}
