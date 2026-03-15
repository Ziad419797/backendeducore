package com.educore.lessonmaterial;

import com.educore.common.CacheNames;
import com.educore.common.SortFields;
import com.educore.common.SortValidator;
import com.educore.dtocourse.request.LessonMaterialCreateRequest;
import com.educore.dtocourse.request.LessonMaterialUpdateRequest;
import com.educore.dtocourse.response.LessonMaterialResponse;
import com.educore.exception.ResourceNotFoundException;
import com.educore.lesson.LessonRepository;
import com.educore.dtocourse.mapper.LessonMaterialMapper;
import com.educore.lesson.Week;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@CacheConfig(cacheNames = CacheNames.MATERIALS)  // cache name افتراضي
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LessonMaterialService {

    private final LessonMaterialRepository materialRepository;
    private final LessonRepository lessonRepository;
    private final LessonMaterialMapper materialMapper;
    private final SortValidator sortValidator;

    // ================= CREATE =================
    @CacheEvict(value = {
            CacheNames.MATERIALS,
            CacheNames.MATERIALS_PAGES,
            CacheNames.LESSONS,        // ⚠️ لأن المواد تظهر في الدروس
            CacheNames.LESSONS_PAGES,
            CacheNames.LESSONS_BY_SESSION
    }, allEntries = true)
    public LessonMaterialResponse createMaterial(LessonMaterialCreateRequest request) {
        log.info("Creating material '{}' for weeks {}", request.getFileName(), request.getWeekIds());

        var weeksList = lessonRepository.findAllById(request.getWeekIds());
        if (weeksList.isEmpty()) {
            throw new ResourceNotFoundException("No valid weeks found for ids " + request.getWeekIds());
        }

        var material = materialMapper.toEntity(request);

        for (Week week : weeksList) {
            material.getWeeks().add(week);
            week.getMaterials().add(material);
        }

        materialRepository.save(material);

        return materialMapper.toResponse(material);
    }

    // ================= UPDATE =================
    @CacheEvict(value = {
            CacheNames.MATERIALS,
            CacheNames.MATERIALS_PAGES,
            CacheNames.LESSONS,        // ⚠️ لأن المواد تظهر في الدروس
            CacheNames.LESSONS_PAGES,
            CacheNames.LESSONS_BY_SESSION
    }, allEntries = true)
    public LessonMaterialResponse updateMaterial(Long id, LessonMaterialUpdateRequest request) {
        log.info("Updating material id {}", id);

        var material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id " + id));

        materialMapper.updateEntityFromRequest(request, material);

        return materialMapper.toResponse(material);
    }

    // ================= DELETE =================

    @CacheEvict(value = {
            CacheNames.MATERIALS,
            CacheNames.MATERIALS_PAGES,
            CacheNames.LESSONS,        // ⚠️ لأن المواد تظهر في الدروس
            CacheNames.LESSONS_PAGES,
            CacheNames.LESSONS_BY_SESSION
    }, allEntries = true)
    public void deleteMaterial(Long id) {
        log.info("Deleting material id {}", id);

        var material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id " + id));

        material.getWeeks().forEach(week -> week.getMaterials().remove(material));
        materialRepository.delete(material);
    }

    // ================= GET BY ID =================
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.MATERIALS, key = "#id")
    public LessonMaterialResponse getMaterialById(Long id) {
        log.info("Fetching material id {}", id);

        var material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id " + id));

        return materialMapper.toResponse(material);
    }

    // ================= GET BY WEEK =================
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.MATERIALS_PAGES, key = "'week-' + #weekId + '-' + #pageable.pageNumber")
    public Page<LessonMaterialResponse> getMaterialsByWeek(Long weekId, Pageable pageable) {
        sortValidator.validate(pageable, SortFields.LESSONMaterial);

        if (!lessonRepository.existsById(weekId)) {
            throw new ResourceNotFoundException("Week not found with id " + weekId);
        }

        return materialRepository.findByWeeksId(weekId, pageable)
                .map(materialMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = {
            CacheNames.MATERIALS,
            CacheNames.MATERIALS_PAGES,
            CacheNames.LESSONS,        // ⚠️ لأن المواد تظهر في الدروس
            CacheNames.LESSONS_PAGES,
            CacheNames.LESSONS_BY_SESSION
    }, allEntries = true)
    public void toggleMaterialStatus(Long id) {
        var material = materialRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));

        material.setActive(!material.isActive());
        materialRepository.save(material);
    }
}
