package com.educore.assignment;

import com.educore.assignment.assignmentQuestion.AssignmentQuestion;
import com.educore.lesson.Week;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE assignments SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description; // تعليمات الواجب

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    @JsonIgnore
    private Week week;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"assignment", "hibernateLazyInitializer", "handler"})
    private Set<AssignmentQuestion> questions = new HashSet<>();

    private LocalDateTime deadline; // موعد تسليم الواجب

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean deleted = false;
}