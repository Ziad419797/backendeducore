package com.educore.lessonmaterial;
import com.educore.lesson.Week;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "lesson_materials",
        indexes = {
                @Index(name = "idx_material_active", columnList = "active")
        }
)
@SQLDelete(sql = "UPDATE lesson_materials SET active = false WHERE id = ?")
@Where(clause = "active = true")@org.hibernate.annotations.Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MaterialType materialType;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;  // ✅ أضف هذا السطر

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private Integer downloadCount = 0;

    @Column(name = "is_preview", nullable = false)
    @Builder.Default
    private Boolean preview = false;

    private Long durationSeconds;

    /*
     * عكس العلاقة مع Week
     */
    @Builder.Default
    @JsonIgnoreProperties("materials")
    @ManyToMany(mappedBy = "materials", fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Week> weeks = new HashSet<>();
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

//    @PrePersist
//    protected void onCreate() {
//        LocalDateTime now = LocalDateTime.now();
//        this.createdAt = now;
//        this.updatedAt = now;
//    }
    @Column(name = "last_downloaded_at")
    private LocalDateTime lastDownloadedAt;
//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }

    public String getFileExtension() {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }

    public boolean isImage() {
        return materialType != null && materialType.isImage();
    }

    public boolean isDocument() {
        return materialType != null && materialType.isDocument();
    }

    public boolean isVideo() {
        return materialType != null && materialType.isVideo();
    }

    public boolean isAudio() {
        return materialType != null && materialType.isAudio();
    }

    public boolean isArchive() {
        return materialType != null && materialType.isArchive();
    }

    public void incrementDownload() {
        this.downloadCount++;
        this.lastDownloadedAt = LocalDateTime.now();
    }

    public void setLastDownloadedAt(LocalDateTime lastDownloadedAt) {
        this.lastDownloadedAt = lastDownloadedAt;
    }

}
