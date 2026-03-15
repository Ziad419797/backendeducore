package com.educore.quiz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @EntityGraph(attributePaths = {"questions"})
    Optional<Quiz> findWithQuestionsById(Long id);

    @EntityGraph(attributePaths = {"questions"})
    Page<Quiz> findByWeekId(Long weekId, Pageable pageable);
    @EntityGraph(attributePaths = {"questions"})
    Optional<Quiz> findWithQuestionsByIdAndDeletedFalse(Long id);

}
