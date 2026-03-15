package com.educore.assignment;
import com.educore.student.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity
@Table(name = "student_assignment_attempts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"assignment_id", "student_id"})
        },
        indexes = {
                @Index(name = "idx_assignment_attempt_assignment", columnList = "assignment_id"),
                @Index(name = "idx_assignment_attempt_student", columnList = "student_id")
        })
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentAssignmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    private Integer score;
    private Boolean submitted;

    @CreationTimestamp
    private LocalDateTime submittedAt;
    private LocalDateTime startedAt;
}