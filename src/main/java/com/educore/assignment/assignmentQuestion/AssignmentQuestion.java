package com.educore.assignment.assignmentQuestion;
import com.educore.assignment.Assignment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "assignment_questions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AssignmentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer mark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    @JsonIgnore
    private Assignment assignment;

    @ElementCollection
    @CollectionTable(name = "assignment_question_options", joinColumns = @JoinColumn(name = "question_id"))
    private List<String> options;

    @Column(nullable = false)
    private String correctAnswer;
    @Builder.Default
    private Boolean deleted = false;

}