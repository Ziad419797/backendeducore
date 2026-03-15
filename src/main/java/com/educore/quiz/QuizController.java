package com.educore.quiz;

import com.educore.auth.GlobalResponse;
import com.educore.dtocourse.request.CreateQuizRequest;
import com.educore.dtocourse.request.SubmitQuizRequest;
import com.educore.dtocourse.response.QuizResponse;
import com.educore.dtocourse.response.QuizResultResponse;
import com.educore.security.JwtUserPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes", description = "إدارة الاختبارات والأسئلة")
public class QuizController {

    private final QuizService quizService;

    @Operation(
            summary = "إنشاء اختبار جديد",
            description = "ينشئ اختبار جديد ويربطه بأسبوع محدد"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "تم إنشاء الاختبار بنجاح",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "400", description = "بيانات غير صالحة"),
            @ApiResponse(responseCode = "404", description = "الأسبوع غير موجود")
    })
    @PostMapping
    public ResponseEntity<GlobalResponse<QuizResponse>> createQuiz(
            @Parameter(description = "بيانات الاختبار", required = true)
            @Valid @RequestBody CreateQuizRequest request
    ) {
        QuizResponse response = quizService.createQuiz(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GlobalResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz created successfully")
                        .data(response)
                        .build());
    }

    @Operation(
            summary = "جلب اختبار محدد",
            description = "يجلب تفاصيل اختبار محدد مع أسئلته"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم جلب الاختبار بنجاح"),
            @ApiResponse(responseCode = "404", description = "الاختبار غير موجود")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<QuizResponse>> getQuiz(
            @Parameter(description = "معرف الاختبار", required = true, example = "1")
            @PathVariable Long id
    ) {
        QuizResponse quiz = quizService.getQuiz(id);

        return ResponseEntity.ok(
                GlobalResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz retrieved successfully")
                        .data(quiz)
                        .build());
    }

    @Operation(
            summary = "جلب اختبارات أسبوع معين",
            description = "يجلب جميع اختبارات أسبوع محدد مع دعم التقسيم"
    )
    @GetMapping("/week/{weekId}")
    public ResponseEntity<GlobalResponse<Page<QuizResponse>>> getQuizzesByWeek(
            @Parameter(description = "معرف الأسبوع", required = true, example = "1")
            @PathVariable Long weekId,

            @Parameter(description = "معلومات التقسيم (صفحة، حجم، ترتيب)")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<QuizResponse> quizzes = quizService.getQuizzesByWeek(weekId, pageable);

        return ResponseEntity.ok(
                GlobalResponse.<Page<QuizResponse>>builder()
                        .success(true)
                        .message("Quizzes retrieved successfully")
                        .data(quizzes)
                        .build());
    }

    @Operation(
            summary = "بدء اختبار",
            description = "يبدأ طالب اختبار معين ويسجل محاولة جديدة"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم بدء الاختبار بنجاح"),
            @ApiResponse(responseCode = "400", description = "الاختبار غير نشط أو الطالب بدأه سابقاً"),
            @ApiResponse(responseCode = "404", description = "الاختبار غير موجود")
    })
    @PostMapping("/{quizId}/start")
    public ResponseEntity<GlobalResponse<QuizResultResponse>> startQuiz(
            @Parameter(description = "معرف الاختبار", required = true, example = "1")
            @PathVariable Long quizId,

            @Parameter(description = "معرف الطالب", required = true, example = "123")
            @RequestHeader("X-Device-Id") String deviceId, // 👈 سواجر هيظهرلها خانة هنا
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        QuizResultResponse result = quizService.startQuiz(quizId);

        return ResponseEntity.ok(
                GlobalResponse.<QuizResultResponse>builder()
                        .success(true)
                        .message("Quiz started successfully")
                        .data(result)
                        .build());
    }

    @Operation(
            summary = "تقديم إجابات الاختبار",
            description = "يقدم الطالب إجاباته للاختبار ويحسب النتيجة"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم تقديم الاختبار بنجاح"),
            @ApiResponse(responseCode = "400", description = "الوقت انتهى أو الطلب غير صالح"),
            @ApiResponse(responseCode = "404", description = "الاختبار أو المحاولة غير موجودة")
    })
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<GlobalResponse<QuizResultResponse>> submitQuiz(
            @Parameter(description = "معرف الاختبار", required = true, example = "1")
            @PathVariable Long quizId,

            @Parameter(description = "إجابات الطالب", required = true)
            @Valid @RequestBody SubmitQuizRequest request,
            @RequestHeader("X-Device-Id") String deviceId, // 👈 سواجر هيظهرلها خانة هنا

            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        QuizResultResponse result = quizService.submitQuiz(quizId, request);

        return ResponseEntity.ok(
                GlobalResponse.<QuizResultResponse>builder()
                        .success(true)
                        .message("Quiz submitted successfully")
                        .data(result)
                        .build());
    }

    @Operation(
            summary = "حذف اختبار",
            description = "يحذف اختبار محدد (soft delete)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم حذف الاختبار بنجاح"),
            @ApiResponse(responseCode = "404", description = "الاختبار غير موجود")
    })
    @DeleteMapping("/{quizId}")
    public ResponseEntity<GlobalResponse<Void>> deleteQuiz(
            @Parameter(description = "معرف الاختبار", required = true, example = "1")
            @PathVariable Long quizId
    ) {
        quizService.deleteQuiz(quizId);

        return ResponseEntity.ok(
                GlobalResponse.<Void>builder()
                        .success(true)
                        .message("Quiz deleted successfully")
                        .build());
    }

}