package com.educore.assignment.assignmentQuestion;
import com.educore.assignment.StudentAssignmentAttempt;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_assignment_answers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentAssignmentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    @JsonIgnore
    private StudentAssignmentAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore
    private AssignmentQuestion question;

    @Column(name = "selected_answer", nullable = false)
    private String selectedAnswer;
}
