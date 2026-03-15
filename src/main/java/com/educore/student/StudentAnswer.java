package com.educore.student;

import com.educore.question.AnswerOption;
import com.educore.question.Question;
import com.educore.quiz.StudentQuizAttempt;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "student_answers",
        indexes = {
                @Index(name = "idx_answer_attempt", columnList = "attempt_id"),
                @Index(name = "idx_answer_question", columnList = "question_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    @JsonIgnore
    private StudentQuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore
    private Question question;
    @Column(name = "selected_answer", nullable = false)
    private String selectedAnswer;

}