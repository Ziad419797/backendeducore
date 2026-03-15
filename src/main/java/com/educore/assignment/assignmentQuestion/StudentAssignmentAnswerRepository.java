package com.educore.assignment.assignmentQuestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentAssignmentAnswerRepository extends JpaRepository<StudentAssignmentAnswer, Long> {

    List<StudentAssignmentAnswer> findByAttemptId(Long attemptId);

    void deleteByAttemptId(Long attemptId);
}