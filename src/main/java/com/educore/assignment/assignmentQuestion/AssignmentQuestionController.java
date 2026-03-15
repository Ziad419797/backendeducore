package com.educore.assignment.assignmentQuestion;

import com.educore.auth.GlobalResponse;
import com.educore.dtocourse.request.CreateAssignmentQuestionRequest;
import com.educore.dtocourse.response.AssignmentQuestionResponse;
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
@RequestMapping("/api/assignment-questions")
@RequiredArgsConstructor
@Tag(name = "Assignment Questions", description = "إدارة الأسئلة الخاصة بالواجبات المدرسية")
public class AssignmentQuestionController {

    private final AssignmentQuestionService questionService;

    @Operation(
            summary = "إضافة سؤال جديد لواجب",
            description = "يضيف سؤال جديد لواجب محدد باستخدام assignmentId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "تم إنشاء السؤال بنجاح",
                    content = @Content(schema = @Schema(implementation = AssignmentQuestionResponse.class))),
            @ApiResponse(responseCode = "400", description = "بيانات غير صالحة"),
            @ApiResponse(responseCode = "404", description = "الواجب غير موجود")
    })
    @PostMapping("/assignment/{assignmentId}")
    public ResponseEntity<GlobalResponse<AssignmentQuestionResponse>> addQuestion(
            @Parameter(description = "معرف الواجب", required = true, example = "1")
            @PathVariable Long assignmentId,

            @Parameter(description = "بيانات السؤال", required = true)
            @Valid @RequestBody CreateAssignmentQuestionRequest request
    ) {
        AssignmentQuestionResponse response = questionService.addQuestion(assignmentId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GlobalResponse.<AssignmentQuestionResponse>builder()
                        .success(true)
                        .message("تم إضافة السؤال للواجب بنجاح")
                        .data(response)
                        .build());
    }

    @Operation(
            summary = "جلب أسئلة واجب معين",
            description = "يجلب جميع أسئلة واجب محدد مع دعم التقسيم والصفحات"
    )
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<GlobalResponse<Page<AssignmentQuestionResponse>>> getQuestions(
            @Parameter(description = "معرف الواجب", required = true, example = "1")
            @PathVariable Long assignmentId,

            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<AssignmentQuestionResponse> questions = questionService.getQuestions(assignmentId, pageable);

        return ResponseEntity.ok(
                GlobalResponse.<Page<AssignmentQuestionResponse>>builder()
                        .success(true)
                        .message("تم جلب أسئلة الواجب بنجاح")
                        .data(questions)
                        .build());
    }

    @Operation(summary = "جلب سؤال واجب محدد", description = "يجلب تفاصيل سؤال محدد باستخدام المعرف")
    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<AssignmentQuestionResponse>> getQuestion(
            @Parameter(description = "معرف السؤال", required = true, example = "1")
            @PathVariable Long id
    ) {
        AssignmentQuestionResponse question = questionService.getQuestion(id);

        return ResponseEntity.ok(
                GlobalResponse.<AssignmentQuestionResponse>builder()
                        .success(true)
                        .message("تم جلب بيانات السؤال بنجاح")
                        .data(question)
                        .build());
    }

    @Operation(summary = "تحديث سؤال واجب", description = "يحدث بيانات سؤال محدد في واجب")
    @PutMapping("/{id}")
    public ResponseEntity<GlobalResponse<AssignmentQuestionResponse>> updateQuestion(
            @Parameter(description = "معرف السؤال", required = true, example = "1")
            @PathVariable Long id,

            @Valid @RequestBody CreateAssignmentQuestionRequest request
    ) {
        AssignmentQuestionResponse updated = questionService.updateQuestion(id, request);

        return ResponseEntity.ok(
                GlobalResponse.<AssignmentQuestionResponse>builder()
                        .success(true)
                        .message("تم تحديث السؤال بنجاح")
                        .data(updated)
                        .build());
    }

    @Operation(summary = "حذف سؤال واجب", description = "يحذف سؤال محدد من الواجب")
    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<Void>> deleteQuestion(
            @Parameter(description = "معرف السؤال", required = true, example = "1")
            @PathVariable Long id
    ) {
        questionService.deleteQuestion(id);

        return ResponseEntity.ok(
                GlobalResponse.<Void>builder()
                        .success(true)
                        .message("تم حذف السؤال بنجاح")
                        .build());
    }

    @Operation(summary = "حذف جميع أسئلة واجب", description = "يحذف جميع أسئلة واجب محدد")
    @DeleteMapping("/assignment/{assignmentId}/all")
    public ResponseEntity<GlobalResponse<Void>> deleteAllByAssignment(
            @Parameter(description = "معرف الواجب", required = true, example = "1")
            @PathVariable Long assignmentId
    ) {
        questionService.deleteAllByAssignment(assignmentId);

        return ResponseEntity.ok(
                GlobalResponse.<Void>builder()
                        .success(true)
                        .message("تم حذف جميع أسئلة الواجب بنجاح")
                        .build());
    }
}