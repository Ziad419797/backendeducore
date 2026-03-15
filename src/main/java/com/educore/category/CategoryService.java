package com.educore.category;

import com.educore.common.CacheNames;
import com.educore.common.SortFields;
import com.educore.common.SortValidator;
import com.educore.dtocourse.mapper.CategoryMapper;
import com.educore.dtocourse.request.CategoryCreateRequest;
import com.educore.dtocourse.request.CategoryUpdateRequest;
import com.educore.dtocourse.response.CategoryResponse;
import com.educore.exception.ResourceNotFoundException;
import com.educore.level.LevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@CacheConfig(cacheNames = CacheNames.CATEGORIES)
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;
    private final CategoryMapper categoryMapper;
    private final SortValidator sortValidator;

    @Caching(evict = {
            @CacheEvict(value = CacheNames.CATEGORIES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true)
    })
    public CategoryResponse createCategory(CategoryCreateRequest request) {

        log.info("Creating category '{}' for level {}", request.getName(), request.getLevelId());

        var level = levelRepository.findById(request.getLevelId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Level not found with id " + request.getLevelId()));

        if (categoryRepository.existsByNameAndLevelId(request.getName(), request.getLevelId())) {
            throw new IllegalArgumentException("Category with same name already exists in this level");
        }

        var category = categoryMapper.toEntity(request);
        category.setLevel(level);

        categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CATEGORIES, key = "#id"),
            @CacheEvict(value = CacheNames.CATEGORIES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true)
    })
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {

        log.info("Updating category id {}", id);

        var category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id " + id));

        categoryMapper.updateEntityFromRequest(request, category);

        return categoryMapper.toResponse(category);
    }
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CATEGORIES, key = "#id"),
            @CacheEvict(value = CacheNames.CATEGORIES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES, allEntries = true)
    })
    public void deleteCategory(Long id) {

        log.info("Deleting category id {}", id);

        var category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id " + id));

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORIES, key = "#id")
    public CategoryResponse getCategoryById(Long id) {

        var category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id " + id));

        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORIES_PAGES, key = "'level-' + #levelId + '-' + #pageable.pageNumber")
    public Page<CategoryResponse> getCategoriesByLevel(Long levelId, Pageable pageable) {

        if (!levelRepository.existsById(levelId)) {
            throw new ResourceNotFoundException("Level not found with id " + levelId);
        }
        sortValidator.validate(pageable, SortFields.CATEGORY);

        return categoryRepository.findByLevelId(levelId, pageable)
                .map(categoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORIES_PAGES, key = "'all-' + #pageable.pageNumber")
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        sortValidator.validate(pageable, SortFields.CATEGORY);

        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    // ================= active =================
    // CategoryService.java

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CATEGORIES, key = "#id"),
            @CacheEvict(value = CacheNames.CATEGORIES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES, allEntries = true)
    })
    public void toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));

        boolean newStatus = !category.isActive();
        category.setActive(newStatus);

        log.info("Category '{}' status changed to: {}", category.getName(), newStatus ? "ACTIVE" : "INACTIVE");
        categoryRepository.save(category);
    }
}
