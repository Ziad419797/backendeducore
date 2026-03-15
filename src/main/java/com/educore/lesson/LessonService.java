package com.educore.lesson;

import com.educore.common.CacheNames;
import com.educore.common.SortFields;
import com.educore.common.SortValidator;
import com.educore.dtocourse.mapper.WeekMapper;
import com.educore.dtocourse.request.LessonUpdateRequest;
import com.educore.dtocourse.request.WeekCreateRequest;
import com.educore.dtocourse.response.WeekResponse;
import com.educore.dtocourse.response.WeekSummaryResponse;
import com.educore.exception.ResourceNotFoundException;
import com.educore.unit.Session;
import com.educore.unit.SeccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
@CacheConfig(cacheNames = CacheNames.LESSONS)  // cache name افتراضي
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SeccionRepository seccionRepository;
    private final WeekMapper weekMapper;
    private final SortValidator sortValidator;

    // ================= CREATE =================
    @CacheEvict(value = {
            CacheNames.LESSONS,           // للدرس الواحد
            CacheNames.LESSONS_PAGES,     // لكل الصفحات
            CacheNames.LESSONS_BY_SESSION // لكل الجلسات
    }, allEntries = true)
    public WeekResponse createLesson(WeekCreateRequest request) {

        log.info("Creating week '{}' for sessions {}",
                request.getTitle(), request.getSessionIds());

        Set<Session> sessions = new HashSet<>(
                seccionRepository.findAllById(request.getSessionIds())
        );

        if (sessions.size() != request.getSessionIds().size()) {
            throw new ResourceNotFoundException("One or more sessions not found");
        }

        var week = weekMapper.toEntity(request);
        week.setSessions(sessions);

        lessonRepository.save(week);

        return weekMapper.toResponse(week);
    }

    // ================= UPDATE =================
    @CacheEvict(value = {
            CacheNames.LESSONS,           // للدرس الواحد
            CacheNames.LESSONS_PAGES,     // لكل الصفحات
            CacheNames.LESSONS_BY_SESSION // لكل الجلسات
    }, allEntries = true)
    public WeekResponse updateLesson(Long id, LessonUpdateRequest request) {

        log.info("Updating week id {}", id);

        var week = lessonRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Week not found with id " + id));

        weekMapper.updateEntityFromRequest(request, week);

        return weekMapper.toResponse(week);
    }

    // ================= DELETE =================
    @CacheEvict(value = {
            CacheNames.LESSONS,           // للدرس الواحد
            CacheNames.LESSONS_PAGES,     // لكل الصفحات
            CacheNames.LESSONS_BY_SESSION // لكل الجلسات
    }, allEntries = true)
    public void deleteLesson(Long id) {

        log.info("Deleting week id {}", id);

        var week = lessonRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Week not found with id " + id));

        lessonRepository.delete(week);
    }

    // ================= GET BY ID =================

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.LESSONS, key = "#id")
    public WeekResponse getLessonById(Long id) {
        log.info("Fetching week id {}", id);

        var week = lessonRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Week not found with id " + id));

        return weekMapper.toResponse(week);
    }

    // ================= GET ALL =================

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.LESSONS_PAGES, key = "'all-' + #pageable.pageNumber")
    public Page<WeekResponse> getAllLessons(Pageable pageable) {

        sortValidator.validate(pageable, SortFields.WEEK);

        return lessonRepository.findAll(pageable)
                .map(weekMapper::toResponse);
    }

    // ================= GET BY SESSION =================

    @Transactional(readOnly = true)
    @Cacheable(value =  CacheNames.LESSONS_BY_SESSION, key = "'session-' + #sessionId + '-' + #pageable.pageNumber")
    public Page<WeekSummaryResponse> getLessonsBySession(
            Long sessionId,
            Pageable pageable) {

        if (!seccionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException(
                    "Session not found with id " + sessionId);
        }

        sortValidator.validate(pageable, SortFields.WEEK);

        Page<Week> page =
                lessonRepository.findBySessionIdAndActiveTrue(sessionId, pageable);

        return page.map(weekMapper::toSummaryResponse);
    }

    // LessonService.java

    @Transactional
    @CacheEvict(value = {
            CacheNames.LESSONS,           // للدرس الواحد
            CacheNames.LESSONS_PAGES,     // لكل الصفحات
            CacheNames.LESSONS_BY_SESSION // لكل الجلسات
    }, allEntries = true)    public void toggleLessonStatus(Long id) {
        log.info("Toggling status for lesson (week) id: {}", id);

        // نستخدم الـ Native Query عشان نجيب الدرس حتى لو inactive
        Week week = lessonRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id " + id));

        boolean newStatus = !week.isActive();
        week.setActive(newStatus);

        log.info("Lesson '{}' is now {}", week.getTitle(), newStatus ? "ACTIVE" : "INACTIVE");
        lessonRepository.save(week);
    }
}
