package com.educore.assignment;

import com.educore.dtocourse.mapper.AssignmentResultMapper;
import com.educore.dtocourse.response.AssignmentResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignment-attempts")
@RequiredArgsConstructor
@Tag(name = "Assignment Attempts", description = "إدارة نتائج وإحصائيات تسليم الواجبات")
public class AssignmentAttemptController {

    private final AssignmentAttemptService attemptService;
    private final AssignmentResultMapper resultMapper;

    @GetMapping("/{attemptId}")
    @Operation(summary = "جلب محاولة واجب محددة", description = "يرجع تفاصيل تسليم واجب معين للطالب بالرقم التعريفي")
    public ResponseEntity<AssignmentResultResponse> getAttemptById(@PathVariable Long attemptId) {
        StudentAssignmentAttempt attempt = attemptService.getAttemptById(attemptId);
        return ResponseEntity.ok(resultMapper.toDetailResponse(attempt, attempt.getAssignment()));
    }

    @GetMapping("/student/{studentId}/assignment/{assignmentId}")
    @Operation(summary = "جلب محاولة الطالب لواجب معين")
    public ResponseEntity<AssignmentResultResponse> getAttemptByStudentAndAssignment(
            @PathVariable Long studentId,
            @PathVariable Long assignmentId
    ) {
        StudentAssignmentAttempt attempt = attemptService.getAttemptByStudentAndAssignment(studentId, assignmentId);
        return ResponseEntity.ok(resultMapper.toDetailResponse(attempt, attempt.getAssignment()));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "جلب جميع واجبات الطالب المسلمة", description = "قائمة بجميع نتائج الواجبات التي قام الطالب بحلها")
    public ResponseEntity<Page<AssignmentResultResponse>> getAttemptsByStudent(
            @PathVariable Long studentId,
            @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<StudentAssignmentAttempt> attemptsPage = attemptService.getAttemptsByStudent(studentId, pageable);

        // تحويل الصفحة من Entity إلى DTO
        Page<AssignmentResultResponse> responsePage = attemptsPage.map(attempt ->
                resultMapper.toDetailResponse(attempt, attempt.getAssignment())
        );

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/assignment/{assignmentId}")
    @Operation(summary = "جلب جميع تسليمات الطلاب لواجب معين", description = "يستخدمه المدرس لمتابعة درجات الطلاب في واجب محدد")
    public ResponseEntity<Page<AssignmentResultResponse>> getAttemptsByAssignment(
            @PathVariable Long assignmentId,
            @PageableDefault(size = 10, sort = "score", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<StudentAssignmentAttempt> attemptsPage = attemptService.getAttemptsByAssignment(assignmentId, pageable);

        if (attemptsPage.isEmpty()) {
            return ResponseEntity.ok(Page.empty());
        }

        // تحسين الأداء: جلب بيانات الواجب من أول محاولة (بما أن الصفحة كلها لنفس الواجب)
        Assignment assignment = attemptsPage.getContent().get(0).getAssignment();

        Page<AssignmentResultResponse> responsePage = attemptsPage.map(attempt ->
                resultMapper.toDetailResponse(attempt, assignment)
        );

        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/assignment/{assignmentId}/statistics")
    @Operation(summary = "جلب إحصائيات الواجب", description = "يرجع متوسط الدرجات وعدد التسليمات لواجب معين")
    public ResponseEntity<AssignmentAttemptService.AssignmentStatistics> getAssignmentStatistics(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(attemptService.getAssignmentStatistics(assignmentId));
    }

    @DeleteMapping("/{attemptId}")
    @Operation(summary = "حذف محاولة تسليم واجب (للمسؤولين فقط)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "تم الحذف بنجاح"),
            @ApiResponse(responseCode = "404", description = "المحاولة غير موجودة")
    })
    public ResponseEntity<Void> deleteAttempt(@PathVariable Long attemptId) {
        attemptService.deleteAttempt(attemptId);
        return ResponseEntity.noContent().build();
    }
}