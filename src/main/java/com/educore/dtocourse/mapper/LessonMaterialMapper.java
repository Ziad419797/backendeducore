package com.educore.dtocourse.mapper;


import com.educore.dtocourse.request.LessonMaterialCreateRequest;
import com.educore.dtocourse.request.LessonMaterialUpdateRequest;
import com.educore.dtocourse.response.LessonMaterialResponse;
import com.educore.lesson.Week;
import com.educore.lessonmaterial.LessonMaterial;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface LessonMaterialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "weeks", ignore = true)
    @Mapping(target = "downloadCount", constant = "0")
    LessonMaterial toEntity(LessonMaterialCreateRequest request);

    @Mapping(target = "weekIds", expression = "java(mapWeeksToIds(material.getWeeks()))")
    LessonMaterialResponse toResponse(LessonMaterial material);

    default Set<Long> mapWeeksToIds(Set<Week> weeks) {
        if (weeks == null) return null;
        return weeks.stream().map(Week::getId).collect(Collectors.toSet());
    }

    @Mapping(target = "weeks", ignore = true)
    void updateEntityFromRequest(LessonMaterialUpdateRequest request, @MappingTarget LessonMaterial material);
}
