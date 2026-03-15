package com.educore.dtocourse.request;


import com.educore.lessonmaterial.MaterialType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonMaterialCreateRequest {

    @NotNull
    private MaterialType materialType;

    @NotNull
    private String fileUrl;

    private String fileName;
    private Long fileSize;
    private Boolean preview;
    private Long durationSeconds;

    @NotEmpty(message = "At least one week is required")
    private Set<Long> weekIds;

}

