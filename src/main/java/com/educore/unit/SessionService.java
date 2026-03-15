package com.educore.unit;

import com.educore.common.CacheNames;
import com.educore.common.SortFields;
import com.educore.common.SortValidator;
import com.educore.course.Course;
import com.educore.course.CourseRepository;
import com.educore.dtocourse.mapper.SessionMapper;
import com.educore.dtocourse.request.CreateSessionRequest;
import com.educore.dtocourse.request.UpdateSessionRequest;
import com.educore.dtocourse.response.SessionResponse;
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
@CacheConfig(cacheNames = CacheNames.SESSIONS)
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionService {

    private final SeccionRepository seccionRepository;
    private final CourseRepository courseRepository;
    private final SessionMapper sessionMapper;
    private  final SortValidator SortValidator;
    // ================= CREATE =================
    @Caching(evict = {
            @CacheEvict(value = CacheNames.SESSIONS_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.SESSIONS_BY_COURSE, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true)
    })    public SessionResponse createSession(CreateSessionRequest request) {

        log.info("Creating session '{}' and linking to courses {}",
                request.getTitle(), request.getCourseIds());

        if (seccionRepository.existsByTitle(request.getTitle())) {
            throw new IllegalArgumentException("Session with same title already exists");
        }

        Set<Course> courses =
                new HashSet<>(courseRepository.findAllById(request.getCourseIds()));

        if (courses.size() != request.getCourseIds().size()) {
            throw new ResourceNotFoundException("One or more courses not found");
        }

        var session = sessionMapper.toEntity(request);

        session.setCourses(courses);

        courses.forEach(course ->
                course.getSessions().add(session)
        );

        seccionRepository.save(session);

        return sessionMapper.toResponse(session);
    }

    // ================= UPDATE =================
    @Caching(evict = {
            @CacheEvict(value = CacheNames.SESSIONS, key = "#id"),
            @CacheEvict(value = CacheNames.SESSIONS_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.SESSIONS_BY_COURSE, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true)
    })    public SessionResponse updateSession(Long id, UpdateSessionRequest request) {

        log.info("Updating session id {}", id);

        var session = seccionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Session not found with id " + id));

        sessionMapper.updateEntityFromRequest(request, session);

        return sessionMapper.toResponse(session);
    }

    // ================= DELETE =================
    @Caching(evict = {
            @CacheEvict(value = CacheNames.SESSIONS, key = "#id"),
            @CacheEvict(value = CacheNames.SESSIONS_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.SESSIONS_BY_COURSE, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheNames.LESSONS_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.LESSONS_BY_SESSION, allEntries = true)
    })    public void deleteSession(Long id) {

        log.info("Deleting session id {}", id);

        var session = seccionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Session not found with id " + id));

        // فك العلاقات ManyToMany
        session.getCourses().forEach(course ->
                course.getSessions().remove(session)
        );

        seccionRepository.delete(session);
    }

    // ================= GET =================

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.SESSIONS, key = "#id")
    public SessionResponse getSessionById(Long id) {

        var session = seccionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Session not found with id " + id));

        return sessionMapper.toResponse(session);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.SESSIONS_PAGES, key = "'all-' + #pageable.pageNumber")
    public Page<SessionResponse> getAllSessions(Pageable pageable) {

        SortValidator.validate(pageable, SortFields.SESSION);

        return seccionRepository.findAll(pageable)
                .map(sessionMapper::toResponse);
    }

    @Cacheable(value = CacheNames.SESSIONS_BY_COURSE, key = "#courseId + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<SessionResponse> getSessionsByCourse(Long courseId, Pageable pageable) {

        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id " + courseId);
        }

        SortValidator.validate(pageable, SortFields.SESSION);

        return seccionRepository
                .findByCoursesId(courseId, pageable)
                .map(sessionMapper::toResponse);
    }
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.SESSIONS, key = "#id"),
            @CacheEvict(value = CacheNames.SESSIONS_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.SESSIONS_BY_COURSE, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.COURSES_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheNames.LESSONS_PAGES, allEntries = true),
            @CacheEvict(value = CacheNames.LESSONS_BY_SESSION, allEntries = true)
    })    public void toggleSessionStatus(Long id) {
        Session session = seccionRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        session.setActive(!session.isActive());
        seccionRepository.save(session);
}
}