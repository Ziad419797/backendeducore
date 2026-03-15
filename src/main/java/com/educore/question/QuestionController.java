package com.educore.question;

import com.educore.auth.GlobalResponse;
import com.educore.dtocourse.request.CreateQuestionRequest;
import com.educore.dtocourse.response.QuestionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "إدارة الأسئلة الخاصة بالاختبارات")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(
            summary = "إضافة سؤال جديد لاختبار",
            description = "يضيف سؤال جديد لاختبار محدد باستخدام quizId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "تم إنشاء السؤال بنجاح",
                    content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
            @ApiResponse(responseCode = "400", description = "بيانات غير صالحة"),
            @ApiResponse(responseCode = "404", description = "الاختبار غير موجود")
    })
    @PostMapping("/quiz/{quizId}")
    public ResponseEntity<GlobalResponse<QuestionResponse>> addQuestion(
            @Parameter(description = "معرف الاختبار", required = true, example = "1")
            @PathVariable Long quizId,

            @Parameter(description = "بيانات السؤال", required = true)
            @Valid @RequestBody CreateQuestionRequest request
    ) {
        QuestionResponse response = questionService.addQuestion(quizId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GlobalResponse.<QuestionResponse>builder()
                        .success(true)
                        .message("Question added successfully")
                        .data(response)
                        .build());
    }

    @Operation(
            summary = "جلب أسئلة اختبار معين",
            description = "يجلب جميع أسئلة اختبار محدد مع دعم التقسيم والصفحات"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم جلب الأسئلة بنجاح"),
            @ApiResponse(responseCode = "404", description = "الاختبار غير موجود")
    })
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<GlobalResponse<Page<QuestionResponse>>> getQuestions(
            @Parameter(description = "معرف الاختبار", required = true, example = "1")
            @PathVariable Long quizId,

            @Parameter(description = "معلومات التقسيم (صفحة، حجم، ترتيب)")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<QuestionResponse> questions = questionService.getQuestions(quizId, pageable);

        return ResponseEntity.ok(
                GlobalResponse.<Page<QuestionResponse>>builder()
                        .success(true)
                        .message("Questions retrieved successfully")
                        .data(questions)
                        .build());
    }

    @Operation(
            summary = "جلب سؤال محدد",
            description = "يجلب تفاصيل سؤال محدد باستخدام المعرف"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم جلب السؤال بنجاح"),
            @ApiResponse(responseCode = "404", description = "السؤال غير موجود")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<QuestionResponse>> getQuestion(
            @Parameter(description = "معرف السؤال", required = true, example = "1")
            @PathVariable Long id
    ) {
        QuestionResponse question = questionService.getQuestion(id);

        return ResponseEntity.ok(
                GlobalResponse.<QuestionResponse>builder()
                        .success(true)
                        .message("Question retrieved successfully")
                        .data(question)
                        .build());
    }

    @Operation(
            summary = "تحديث سؤال",
            description = "يحدث بيانات سؤال محدد"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم تحديث السؤال بنجاح"),
            @ApiResponse(responseCode = "400", description = "بيانات غير صالحة"),
            @ApiResponse(responseCode = "404", description = "السؤال غير موجود")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GlobalResponse<QuestionResponse>> updateQuestion(
            @Parameter(description = "معرف السؤال", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "بيانات التحديث", required = true)
            @Valid @RequestBody CreateQuestionRequest request
    ) {
        QuestionResponse updated = questionService.updateQuestion(id, request);

        return ResponseEntity.ok(
                GlobalResponse.<QuestionResponse>builder()
                        .success(true)
                        .message("Question updated successfully")
                        .data(updated)
                        .build());
    }

    @Operation(
            summary = "حذف سؤال",
            description = "يحذف سؤال محدد (soft delete)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم حذف السؤال بنجاح"),
            @ApiResponse(responseCode = "404", description = "السؤال غير موجود")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<Void>> deleteQuestion(
            @Parameter(description = "معرف السؤال", required = true, example = "1")
            @PathVariable Long id
    ) {
        questionService.deleteQuestion(id);

        return ResponseEntity.ok(
                GlobalResponse.<Void>builder()
                        .success(true)
                        .message("Question deleted successfully")
                        .build());
    }

    @Operation(
            summary = "حذف جميع أسئلة اختبار",
            description = "يحذف جميع أسئلة اختبار محدد (soft delete)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم حذف الأسئلة بنجاح"),
            @ApiResponse(responseCode = "404", description = "الاختبار غير موجود")
    })
    @DeleteMapping("/quiz/{quizId}/all")
    public ResponseEntity<GlobalResponse<Void>> deleteAllQuestionsByQuiz(
            @Parameter(description = "معرف الاختبار", required = true, example = "1")
            @PathVariable Long quizId
    ) {
        questionService.deleteAllQuestionsByQuiz(quizId);

        return ResponseEntity.ok(
                GlobalResponse.<Void>builder()
                        .success(true)
                        .message("All questions deleted successfully")
                        .build());
    }
}