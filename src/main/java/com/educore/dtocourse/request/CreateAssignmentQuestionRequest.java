package com.educore.dtocourse.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateAssignmentQuestionRequest {

    @NotBlank(message = "رابط الصورة مطلوب")
    private String imageUrl;

    @NotBlank(message = "وصف السؤال مطلوب")
    private String description;

    @NotNull(message = "الخيارات مطلوبة")
    @Size(min = 2, max = 10, message = "يجب أن يكون عدد الخيارات بين 2 و 10")
    private List<@NotBlank String> options; // قائمة الخيارات (A, B, C, D أو نصوص)

    @NotBlank(message = "الإجابة الصحيحة مطلوبة")
    private String correctAnswer; // النص المطابق للخيار الصحيح

    @NotNull(message = "الدرجة مطلوبة")
    @Min(value = 1, message = "الدرجة يجب أن تكون 1 على الأقل")
    private Integer mark;
}