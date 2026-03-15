package com.educore.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findByLevelId(Long levelId, Pageable pageable);

    boolean existsByNameAndLevelId(String name, Long levelId);

    Optional<Category> findByNameAndLevelId(String name, Long levelId);
    @Query("""
       SELECT c FROM Category c
       JOIN FETCH c.level
       WHERE c.id = :id
       """)
    Optional<Category> findByIdWithLevel(Long id);
    // CategoryRepository.java
    @Query(value = "SELECT * FROM categories WHERE id = :id", nativeQuery = true)
    Optional<Category> findByIdIncludingInactive(@Param("id") Long id);

}
