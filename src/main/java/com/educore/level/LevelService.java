package com.educore.level;
import com.educore.dtocourse.request.LevelCreateRequest;
import com.educore.dtocourse.request.LevelUpdateRequest;
import com.educore.dtocourse.response.LevelResponse;
import com.educore.exception.ResourceNotFoundException;
import com.educore.dtocourse.mapper.LevelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LevelService {

    private final LevelRepository levelRepository;
    private final LevelMapper levelMapper;

    // Create Level
    @CacheEvict(value = "levels", allEntries = true) // يمسح الكاش لو ضفنا مستوى جديد
    @Transactional
    public LevelResponse createLevel(LevelCreateRequest request) {
        log.info("Creating level with name {}", request.getName());
        Level level = levelMapper.toEntity(request);
        levelRepository.save(level);
        return levelMapper.toResponse(level);
    }

    // Update Level
    @Transactional
    @CacheEvict(value = "levels", allEntries = true) // يمسح الكاش لو ضفنا مستوى جديد
    public LevelResponse updateLevel(Long id, LevelUpdateRequest request) {
        log.info("Updating level id {}", id);
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with id " + id));
        level.setName(request.getName());
        levelRepository.save(level);
        return levelMapper.toResponse(level);
    }

    // Delete Level
    @Transactional
    @CacheEvict(value = "levels", allEntries = true) // يمسح الكاش لو ضفنا مستوى جديد
    public void deleteLevel(Long id) {
        log.info("Deleting level id {}", id);
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with id " + id));
        // ✅ مهم جداً: لو الـ Level عنده Categories، ماتحذفوش
        if (!level.getCategories().isEmpty()) {
            throw new IllegalStateException("Cannot delete level with existing categories");
        }
        levelRepository.delete(level);
    }

    // Get Level by Id
    @Transactional(readOnly = true)
    public LevelResponse getLevelById(Long id) {
        log.info("Fetching level id {}", id);
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found with id " + id));
        return levelMapper.toResponse(level);
    }

    // Get All Levels
    @Cacheable(value = "levels", key = "'all'") // كاش لكل المستويات
    @Transactional(readOnly = true)
    public List<LevelResponse> getAllLevels() {
        log.info("Fetching all levels");
        return levelRepository.findAll().stream()
                .map(levelMapper::toResponse)
                .toList();
    }



}

