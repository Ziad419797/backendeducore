package com.educore.unit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeccionRepository extends JpaRepository<Session, Long> {

    Page<Session> findByCoursesId(Long courseId, Pageable pageable);

    boolean existsByTitle(String title);
    // 👈 لازم تضيفي دي عشان الـ Toggle يشتغل
    @Query(value = "SELECT * FROM sessions WHERE id = :id", nativeQuery = true)
    Optional<Session> findByIdIncludingInactive(@Param("id") Long id);
}
