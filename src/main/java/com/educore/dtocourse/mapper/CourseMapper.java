package com.educore.dtocourse.mapper;

import com.educore.category.Category;
import com.educore.course.Course;
import com.educore.dtocourse.request.CreateCourseRequest;
import com.educore.dtocourse.request.UpdateCourseRequest;
import com.educore.dtocourse.response.CourseResponse;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    Course toEntity(CreateCourseRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateCourseRequest request, @MappingTarget Course course);

    @Mapping(target = "categoryIds", expression = "java(mapCategoriesToIds(course.getCategories()))")
    CourseResponse toResponse(Course course);

    default Set<Long> mapCategoriesToIds(Set<Category> categories) {
        if (categories == null) return null;
        return categories.stream().map(Category::getId).collect(Collectors.toSet());
    }
}
