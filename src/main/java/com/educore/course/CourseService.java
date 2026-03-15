package com.educore.course;

import com.educore.category.Category;
import com.educore.category.CategoryRepository;
import com.educore.common.CacheNames;
import com.educore.common.SortFields;
import com.educore.common.SortValidator;
import com.educore.dtocourse.mapper.CourseMapper;
import com.educore.dtocourse.request.CreateCourseRequest;
import com.educore.dtocourse.request.UpdateCourseRequest;
import com.educore.dtocourse.response.CourseResponse;
import com.educore.exception.ResourceNotFoundException;
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

import java.util.HashSet;
import java.util.Set;
@CacheConfig(cacheNames = CacheNames.COURSES)
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CourseMapper courseMapper;
    private final SortValidator sortValidator;

    // ================= CREATE =================
    @Caching(evict = {
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORIES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.SESSIONS_BY_COURSE, allEntries = true)
    })    public CourseResponse
    createCourse(CreateCourseRequest request) {

        log.info("Creating course '{}' and linking to categories {}",
                request.getTitle(), request.getCategoryIds());

        if (courseRepository.existsByTitle(request.getTitle())) {
            throw new IllegalArgumentException("Course with same title already exists");
        }

        // تحميل كل الكاتيجوريز مرة واحدة
        Set<Category> categories =
                new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));

        if (categories.size() != request.getCategoryIds().size()) {
            throw new ResourceNotFoundException("One or more categories not found");
        }

        var course = courseMapper.toEntity(request);

        // ربط العلاقات ManyToMany
        course.setCategories(categories);

        categories.forEach(category ->
                category.getCourses().add(course)
        );

        courseRepository.save(course);

        return courseMapper.toResponse(course);
    }

    // ================= UPDATE =================
    @Caching(evict = {
            @CacheEvict(value = CacheNames.COURSES, key = "#id"),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORIES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.SESSIONS_BY_COURSE, allEntries = true)
    })
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {

        log.info("Updating course id {}", id);

        var course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id " + id));

        courseMapper.updateEntityFromRequest(request, course);

        return courseMapper.toResponse(course);
    }

    // ================= DELETE =================
    @Caching(evict = {
            @CacheEvict(value = CacheNames.COURSES, key = "#id"),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORIES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.SESSIONS_BY_COURSE, allEntries = true),
            @CacheEvict(value = CacheNames.SESSIONS_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.LESSONS_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.LESSONS_BY_SESSION, allEntries = true)
    })
    public void deleteCourse(Long id) {

        log.info("Deleting course id {}", id);

        var course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id " + id));

        // تنظيف العلاقات قبل الحذف
        course.getCategories().forEach(category ->
                category.getCourses().remove(course)
        );

        courseRepository.delete(course);
    }

    // ================= GET =================
    @Cacheable(value = CacheNames.COURSES, key = "#id")
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {

        var course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id " + id));

        return courseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.COURSES_PAGES, key = "'all-' + #pageable.pageNumber")
    public Page<CourseResponse> getAllCourses(Pageable pageable) {

        sortValidator.validate(pageable, SortFields.COURSE);

        return courseRepository.findAll(pageable)
                .map(courseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.COURSES_BY_CATEGORY, key = "#categoryId + '-' + #pageable.pageNumber")
    public Page<CourseResponse> getCoursesByCategory(Long categoryId, Pageable pageable) {

        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id " + categoryId);
        }

        sortValidator.validate(pageable, SortFields.COURSE);

        return courseRepository
                .findByCategoriesId(categoryId, pageable)
                .map(courseMapper::toResponse);
    }

    // ================= active =================

// CourseService.java

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.COURSES, key = "#id"),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORIES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.SESSIONS_BY_COURSE, allEntries = true),
            @CacheEvict(value = CacheNames.SESSIONS_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.LESSONS_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.LESSONS_BY_SESSION, allEntries = true)
    })
    public void toggleCourseStatus(Long id) {
        // ملحوظة: بما أننا نستخدم @Where(clause = "active = true")
        // الـ Repository العادي لن يجد الكورس إذا كان active = false
        // لذلك نحتاج لعمل Query مخصصة في الـ Repository تجلب العنصر بغض النظر عن حالته

        Course course = courseRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        boolean newStatus = !course.isActive();
        course.setActive(newStatus);

        log.info("Course '{}' is now {}", course.getTitle(), newStatus ? "ACTIVE" : "INACTIVE");
        courseRepository.save(course);
    }
}
