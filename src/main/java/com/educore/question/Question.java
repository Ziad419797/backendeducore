package com.educore.question;
import com.educore.quiz.Quiz;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "questions",
        indexes = {
                @Index(name = "idx_question_quiz", columnList = "quiz_id"),
                @Index(name = "idx_question_deleted", columnList = "deleted"),
                @Index(name = "idx_question_mark", columnList = "mark")
        })@SQLDelete(sql = "UPDATE questions SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // صورة السؤال
    @Column(nullable = false)
    private String imageUrl;
    @Column(nullable = false)
     private  String description;
    @Column(nullable = false)
    private Integer mark; // درجة السؤال

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties("questions")
    @JsonIgnore
    private Quiz quiz;
    @ElementCollection
    @CollectionTable(
            name = "question_options",
            joinColumns = @JoinColumn(name = "question_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "option_key"})
    )
    @Column(name = "option_value")
    private List<String> options;

    @Column(nullable = false)
    private String correctAnswer;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    // دالة مساعدة للتحقق من الإجابة
    public boolean isCorrectAnswer(String selectedOption) {
        if (selectedOption == null || correctAnswer == null) return false;
        return correctAnswer.equals(selectedOption);
    }

    // دالة مساعدة للحصول على عدد الخيارات
    public int getOptionsCount() {
        return options != null ? options.size() : 0;
    }
}
