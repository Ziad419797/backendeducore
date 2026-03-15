package com.educore.lesson;

import com.educore.auth.GlobalResponse;
import com.educore.dtocourse.request.LessonUpdateRequest;
import com.educore.dtocourse.request.WeekCreateRequest;
import com.educore.dtocourse.response.WeekResponse;
import com.educore.dtocourse.response.WeekSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weeks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Weeks", description = "إدارة الأسابيع داخل السيشنات")
public class LessonController {

    private final LessonService lessonService;

    // ================= CREATE =================

    @Operation(summary = "إضافة أسبوع")
    @PostMapping
    public ResponseEntity<WeekResponse> createWeek(
            @Validated @RequestBody WeekCreateRequest request) {

        log.info("API [POST] /api/weeks");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lessonService.createLesson(request));
    }

    // ================= UPDATE =================

    @PutMapping("/{id}")
    public ResponseEntity<WeekResponse> updateWeek(
            @PathVariable Long id,
            @Validated @RequestBody LessonUpdateRequest request) {

        log.info("API [PUT] /api/weeks/{}", id);

        return ResponseEntity.ok(
                lessonService.updateLesson(id, request));
    }

    // ================= DELETE =================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWeek(@PathVariable Long id) {

        log.info("API [DELETE] /api/weeks/{}", id);

        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    // ================= GET BY ID =================

    @GetMapping("/{id}")
    public ResponseEntity<WeekResponse> getWeekById(@PathVariable Long id) {

        log.info("API [GET] /api/weeks/{}", id);

        return ResponseEntity.ok(
                lessonService.getLessonById(id));
    }

    // ================= GET ALL =================

    @GetMapping
    public ResponseEntity<Page<WeekResponse>> getAllWeeks(
            Pageable pageable) {

        log.info("API [GET] /api/weeks");

        return ResponseEntity.ok(
                lessonService.getAllLessons(pageable));
    }

    // ================= GET BY SESSION =================

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Page<WeekSummaryResponse>> getWeeksBySession(
            @PathVariable Long sessionId,
            Pageable pageable) {

        log.info("API [GET] /api/weeks/session/{}", sessionId);

        return ResponseEntity.ok(
                lessonService.getLessonsBySession(sessionId, pageable));
    }
    // LessonController.java

    @Operation(summary = "Toggle lesson activation status", description = "إظهار أو إخفاء الدرس للطلاب")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<GlobalResponse<Void>> toggleStatus(@PathVariable Long id) {
        log.info("PATCH /api/lessons/{}/toggle-status", id);

        lessonService.toggleLessonStatus(id);

        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم تغيير حالة الدرس بنجاح")
                .build());
    }
}
