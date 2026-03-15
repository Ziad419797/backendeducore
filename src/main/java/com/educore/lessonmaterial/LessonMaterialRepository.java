package com.educore.lessonmaterial;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {
    // ✅ استخدم @Query زي ما عملنا في LessonRepository
    @Query("""
SELECT DISTINCT m 
FROM LessonMaterial m 
JOIN m.weeks w 
WHERE w.id = :weekId 
AND m.active = true
""")
    Page<LessonMaterial> findByWeeksId(Long weekId, Pageable pageable);
    @Query(value = "SELECT * FROM lesson_materials WHERE id = :id", nativeQuery = true)
    Optional<LessonMaterial> findByIdIncludingInactive(@Param("id") Long id);
//    Page<LessonMaterial> findByWeeksId(Long lessonId, Pageable pageable);
}
