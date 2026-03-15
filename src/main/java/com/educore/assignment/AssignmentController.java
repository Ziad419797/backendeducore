package com.educore.assignment;

import com.educore.auth.GlobalResponse;
import com.educore.dtocourse.request.CreateAssignmentRequest;
import com.educore.dtocourse.request.SubmitAssignmentRequest;
import com.educore.dtocourse.response.AssignmentResponse;
import com.educore.dtocourse.response.AssignmentResultResponse;
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
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "إدارة الواجبات المدرسية وتسليمات الطلاب")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentAttemptService attemptService;

    @Operation(
            summary = "إنشاء واجب جديد",
            description = "ينشئ واجب جديد ويربطه بأسبوع محدد مع تحديد موعد نهائي للتسليم"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "تم إنشاء الواجب بنجاح",
                    content = @Content(schema = @Schema(implementation = AssignmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "بيانات غير صالحة"),
            @ApiResponse(responseCode = "404", description = "الأسبوع غير موجود")
    })
    @PostMapping
    public ResponseEntity<GlobalResponse<AssignmentResponse>> createAssignment(
            @Parameter(description = "بيانات الواجب", required = true)
            @Valid @RequestBody CreateAssignmentRequest request
    ) {
        AssignmentResponse response = assignmentService.createAssignment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GlobalResponse.<AssignmentResponse>builder()
                        .success(true)
                        .message("تم إنشاء الواجب بنجاح")
                        .data(response)
                        .build());
    }

    @Operation(summary = "جلب واجب محدد", description = "يجلب تفاصيل الواجب مع كافة الأسئلة التابعة له")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم جلب الواجب بنجاح"),
            @ApiResponse(responseCode = "404", description = "الواجب غير موجود")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<AssignmentResponse>> getAssignment(
            @Parameter(description = "معرف الواجب", required = true, example = "1")
            @PathVariable Long id
    ) {
        AssignmentResponse assignment = assignmentService.getAssignment(id);

        return ResponseEntity.ok(
                GlobalResponse.<AssignmentResponse>builder()
                        .success(true)
                        .message("تم جلب بيانات الواجب بنجاح")
                        .data(assignment)
                        .build());
    }

    @Operation(summary = "جلب واجبات أسبوع معين", description = "يجلب جميع الواجبات المرتبطة بأسبوع محدد")
    @GetMapping("/week/{weekId}")
    public ResponseEntity<GlobalResponse<Page<AssignmentResponse>>> getAssignmentsByWeek(
            @Parameter(description = "معرف الأسبوع", required = true, example = "1")
            @PathVariable Long weekId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AssignmentResponse> assignments = assignmentService.getAssignmentsByWeek(weekId, pageable);

        return ResponseEntity.ok(
                GlobalResponse.<Page<AssignmentResponse>>builder()
                        .success(true)
                        .message("تم جلب واجبات الأسبوع بنجاح")
                        .data(assignments)
                        .build());
    }

    @Operation(
            summary = "تسليم الواجب",
            description = "يقدم الطالب إجابات الواجب ويتم تصحيحها وحساب النتيجة فوراً"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تم تسليم الواجب بنجاح"),
            @ApiResponse(responseCode = "400", description = "تجاوز الموعد النهائي أو تم التسليم مسبقاً")
    })
    @PostMapping("/{assignmentId}/submit")
    public ResponseEntity<GlobalResponse<AssignmentResultResponse>> submitAssignment(
            @Parameter(description = "معرف الواجب", required = true, example = "1")
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmitAssignmentRequest request,
            @RequestHeader("X-Device-Id") String deviceId,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        AssignmentResultResponse result = assignmentService.submitAssignment(assignmentId, request);

        return ResponseEntity.ok(
                GlobalResponse.<AssignmentResultResponse>builder()
                        .success(true)
                        .message("تم تسليم وتصحيح الواجب بنجاح")
                        .data(result)
                        .build());
    }

    @Operation(summary = "حذف واجب", description = "حذف الواجب بشكل ناعم (Soft Delete)")
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<GlobalResponse<Void>> deleteAssignment(
            @Parameter(description = "معرف الواجب", required = true, example = "1")
            @PathVariable Long assignmentId
    ) {
        assignmentService.deleteAssignment(assignmentId);

        return ResponseEntity.ok(
                GlobalResponse.<Void>builder()
                        .success(true)
                        .message("تم حذف الواجب بنجاح")
                        .build());
    }
}