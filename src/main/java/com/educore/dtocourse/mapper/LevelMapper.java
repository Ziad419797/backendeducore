package com.educore.dtocourse.mapper;

import com.educore.dtocourse.request.LevelCreateRequest;
import com.educore.dtocourse.response.LevelResponse;
import com.educore.level.Level;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LevelMapper {

    // ===== Create =====
    Level toEntity(LevelCreateRequest request);

    // ===== Response =====
    @Mapping(target = "categories", source = "categories")
    LevelResponse toResponse(Level level);

//    // ===== Nested =====
//    List<UnitSummaryResponse> mapUnits(List<Unit> units);
//
//    @Mapping(target = "id", source = "id")
//    UnitSummaryResponse toUnitSummary(Unit unit);
}

