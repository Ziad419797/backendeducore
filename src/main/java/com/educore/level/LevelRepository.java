package com.educore.level;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LevelRepository extends JpaRepository<Level, Long> {
    // ✅ جلب Level مع Categories (لتجنب LazyInitializationException)
    @Query("SELECT l FROM Level l LEFT JOIN FETCH l.categories WHERE l.id = :id")
    Optional<Level> findByIdWithCategories(@Param("id") Long id);

    // ✅ جلب كل المستويات مع Categories
    @Query("SELECT DISTINCT l FROM Level l LEFT JOIN FETCH l.categories")
    List<Level> findAllWithCategories(Pageable pageable);

}

