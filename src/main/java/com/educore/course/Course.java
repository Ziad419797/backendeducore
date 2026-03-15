package com.educore.course;

import com.educore.category.Category;
import com.educore.unit.Session;
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
        name = "courses",
        indexes = {
                @Index(name = "idx_course_active", columnList = "active")
        }
)
@SQLDelete(sql = "UPDATE courses SET active = false WHERE id = ?")
@Where(clause = "active = true")
@Cacheable
@org.hibernate.annotations.Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_category",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"),
    indexes = {
        @Index(name = "idx_course_category_course", columnList = "course_id"),
        @Index(name = "idx_course_category_category", columnList = "category_id")
    }
    )
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE) // 👈 ضيفي ده هنا
    private Set<Category> categories = new HashSet<>();
    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)  // 👈 غيّر هنا
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties("courses")
    private Set<Session> sessions = new HashSet<>();
}
