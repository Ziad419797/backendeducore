package com.educore.dtocourse.mapper;

import com.educore.category.Category;
import com.educore.dtocourse.request.CategoryCreateRequest;
import com.educore.dtocourse.request.CategoryUpdateRequest;
import com.educore.dtocourse.response.CategoryResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CategoryUpdateRequest request, @MappingTarget Category category);
    @Mapping(target = "levelId", source = "level.id")
    CategoryResponse toResponse(Category category);
}
