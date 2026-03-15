package com.educore.quiz;

import com.educore.lesson.Week;
import com.educore.question.Question;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "quizzes",
        indexes = {
                @Index(name = "idx_quiz_week", columnList = "week_id"),
                @Index(name = "idx_quiz_deleted", columnList = "deleted"),
                @Index(name = "idx_quiz_active", columnList = "active")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@SQLDelete(sql = "UPDATE quizzes SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    @JsonIgnore
    private Week week;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("quiz")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Question> questions = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer durationMinutes; // مدة الامتحان
    @Builder.Default
    @Column(nullable = false)
    private Boolean timeRestricted = false;
    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false; // Soft Delete

}
