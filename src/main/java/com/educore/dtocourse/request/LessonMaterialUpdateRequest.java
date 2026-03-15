package com.educore.dtocourse.request;

import com.educore.lessonmaterial.MaterialType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonMaterialUpdateRequest {

    private MaterialType materialType;
    private String fileUrl;
    private String fileName;
    private Boolean preview;
    private Long durationSeconds;
}
