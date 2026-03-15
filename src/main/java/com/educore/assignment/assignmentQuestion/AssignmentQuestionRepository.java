package com.educore.assignment.assignmentQuestion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentQuestionRepository extends JpaRepository<AssignmentQuestion, Long> {

    @Modifying
    @Query("UPDATE AssignmentQuestion q SET q.deleted = true WHERE q.assignment.id = :assignmentId")
    void deleteByAssignmentId(@Param("assignmentId") Long assignmentId);

    Page<AssignmentQuestion> findByAssignmentId(Long assignmentId, Pageable pageable);
}