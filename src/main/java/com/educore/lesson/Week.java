package com.educore.lesson;
import com.educore.assignment.Assignment;
import com.educore.lessonmaterial.LessonMaterial;
import com.educore.quiz.Quiz;
import com.educore.unit.Session;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "weeks",
        indexes = {
                @Index(name = "idx_week_active", columnList = "active")
        }
)
@SQLDelete(sql = "UPDATE weeks SET active = false WHERE id = ?")
@Where(clause = "active = true")
@Cacheable
@org.hibernate.annotations.Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Week {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Column(length = 2000)
    private String description;

    private Integer orderNumber;

    private boolean active = true;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "unit_id", nullable = false)
//    private Unit unit;

    /*
     * ManyToMany مع Session
     */
    @ManyToMany
    @JoinTable(
            name = "session_weeks",
            joinColumns = @JoinColumn(name = "week_id"),
            inverseJoinColumns = @JoinColumn(name = "session_id"),
            indexes = {
                    @Index(name = "idx_week_session_week", columnList = "week_id"),
                    @Index(name = "idx_week_session_session", columnList = "session_id")
            }
    )
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties("weeks")
    private Set<Session> sessions = new HashSet<>();

    /*
     * ManyToMany مع LessonMaterial
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "week_materials",
            joinColumns = @JoinColumn(name = "week_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"),
            indexes = {
                    @Index(name = "idx_week_materials_week", columnList = "week_id"),
                    @Index(name = "idx_week_materials_material", columnList = "material_id")
            }
    )
    @JsonIgnoreProperties("weeks")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<LessonMaterial> materials = new HashSet<>();


    @OneToMany(mappedBy = "week", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Quiz> quizzes = new HashSet<>();

    @OneToMany(mappedBy = "week", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Assignment> assignments = new HashSet<>();

//    @OneToMany(mappedBy = "week", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Assignment> assignments = new HashSet<>();


    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(name = "has_quiz", nullable = false)
    @Builder.Default
    private boolean hasQuiz = false;  // ✅ قيمة افتراضية

//    @PrePersist
//    protected void onCreate() {
//        LocalDateTime now = LocalDateTime.now();
//        this.createdAt = now;
//        this.updatedAt = now;
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }

}
