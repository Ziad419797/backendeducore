package com.educore.quiz;

import com.educore.student.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_quiz_attempts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"quiz_id", "student_id"})
        },
        indexes = {
                @Index(name = "idx_attempt_quiz", columnList = "quiz_id"),
                @Index(name = "idx_attempt_student", columnList = "student_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentQuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    private Integer score;

    private Boolean submitted;

    @CreationTimestamp
    private LocalDateTime submittedAt;

    private LocalDateTime startedAt;

    private LocalDateTime expiresAt;

}
