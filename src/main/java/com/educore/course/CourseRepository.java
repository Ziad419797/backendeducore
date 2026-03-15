package com.educore.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByCategoriesId(Long categoryId, Pageable pageable);

    boolean existsByTitle(String title);
    // CourseRepository.java
    @Query(value = "SELECT * FROM courses WHERE id = :id", nativeQuery = true)
    Optional<Course> findByIdIncludingInactive(@Param("id") Long id);
}
