package com.educore.unit;

import com.educore.course.Course;
import com.educore.lesson.Week;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "sessions",
        indexes = {
                @Index(name = "idx_session_active", columnList = "active")
        }
)
@SQLDelete(sql = "UPDATE sessions SET active = false WHERE id = ?")
@Where(clause = "active = true")@Cacheable
@org.hibernate.annotations.Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private boolean active = true;
    @CreationTimestamp
    private LocalDateTime createdAt ;
    @UpdateTimestamp
    private LocalDateTime updatedAt ;
//    @PreUpdate
//    public void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "session_courses",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"),
            indexes = {
                    @Index(name = "idx_session_course_session", columnList = "session_id"),
                    @Index(name = "idx_session_course_course", columnList = "course_id")
            }
    )
    @JsonIgnoreProperties("sessions")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Course> courses = new HashSet<>();
    // Session.java
    @ManyToMany(mappedBy = "sessions", fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties("sessions")
    private Set<Week> weeks = new HashSet<>();
}
