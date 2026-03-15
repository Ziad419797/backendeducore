package com.educore.lesson;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LessonRepository extends JpaRepository<Week, Long> {
    // ✅ استخدام @Query بدلاً من method naming
    @Query("""
SELECT DISTINCT w 
FROM Week w 
JOIN w.sessions s 
WHERE s.id = :sessionId 
AND w.active = true
""")
    Page<Week> findBySessionId(Long sessionId, Pageable pageable);


    @Query("SELECT w FROM Week w JOIN w.sessions s WHERE s.id = :sessionId AND w.active = true")
    Page<Week> findBySessionIdAndActiveTrue(Long sessionId, Pageable pageable);
    // LessonRepository.java
    @Query(value = "SELECT * FROM weeks WHERE id = :id", nativeQuery = true)
    Optional<Week> findByIdIncludingInactive(@Param("id") Long id);
//    Page<Week> findBySessionId(Long sessionId, Pageable pageable);
//    Page<Week> findBySessions_IdAndActiveTrue(Long sessionId, Pageable pageable);


}
