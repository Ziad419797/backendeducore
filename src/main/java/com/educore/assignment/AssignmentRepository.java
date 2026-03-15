package com.educore.assignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @EntityGraph(attributePaths = {"questions"})
    Optional<Assignment> findWithQuestionsById(Long id);

    @EntityGraph(attributePaths = {"questions"})
    Page<Assignment> findByWeekId(Long weekId, Pageable pageable);

    @EntityGraph(attributePaths = {"questions"})
    Optional<Assignment> findWithQuestionsByIdAndDeletedFalse(Long id);
}