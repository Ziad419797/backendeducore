package com.educore.lessonmaterial;

import com.educore.auth.GlobalResponse;
import com.educore.dtocourse.request.LessonMaterialCreateRequest;
import com.educore.dtocourse.request.LessonMaterialUpdateRequest;
import com.educore.dtocourse.response.LessonMaterialResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lesson Materials", description = "إدارة ملفات الدروس (فيديوهات - PDF - الخ)")
public class LessonMaterialController {

    private final LessonMaterialService materialService;

    @Operation(summary = "إضافة ملف جديد للدرس", description = "يستخدم لإضافة فيديو أو PDF أو أي مادة تعليمية داخل درس")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "تم إنشاء المادة بنجاح"),
            @ApiResponse(responseCode = "404", description = "الدرس غير موجود"),
            @ApiResponse(responseCode = "400", description = "بيانات غير صحيحة")
    })
    @PostMapping
    public ResponseEntity<LessonMaterialResponse> createMaterial(
            @Validated @RequestBody LessonMaterialCreateRequest request) {
        log.info("API /api/materials [POST] called");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(materialService.createMaterial(request));
    }

    @Operation(summary = "تحديث مادة تعليمية", description = "تحديث بيانات المادة (الرابط – الاسم – المعاينة – المدة)")
    @PutMapping("/{id}")
    public ResponseEntity<LessonMaterialResponse> updateMaterial(
            @PathVariable Long id,
            @Validated @RequestBody LessonMaterialUpdateRequest request) {
        log.info("API /api/materials/{} [PUT] called", id);
        return ResponseEntity.ok(materialService.updateMaterial(id, request));
    }

    @Operation(summary = "حذف مادة تعليمية", description = "حذف مادة تعليمية نهائيًا")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        log.info("API /api/materials/{} [DELETE] called", id);
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "جلب مادة تعليمية", description = "إرجاع تفاصيل مادة تعليمية واحدة")
    @GetMapping("/{id}")
    public ResponseEntity<LessonMaterialResponse> getMaterialById(@PathVariable Long id) {
        log.info("API /api/materials/{} [GET] called", id);
        return ResponseEntity.ok(materialService.getMaterialById(id));
    }

    @Operation(summary = "جلب مواد درس", description = "إرجاع كل المواد التابعة لدرس معين مع Pagination")
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<Page<LessonMaterialResponse>> getMaterialsByWeek(
            @PathVariable Long lessonId,
            Pageable pageable) {
        log.info("API /api/materials/lesson/{} [GET] called with pagination", lessonId);
        return ResponseEntity.ok(materialService.getMaterialsByWeek(lessonId, pageable));
    }

// ================= TOGGLE STATUS (The new method) =================

    @Operation(summary = "Toggle material activation", description = "إظهار أو إخفاء ملف/فيديو معين للطلاب")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<GlobalResponse<Void>> toggleStatus(@PathVariable Long id) {
        log.info("PATCH /api/materials/{}/toggle-status", id);

        materialService.toggleMaterialStatus(id);

        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم تغيير حالة الملف بنجاح")
                .build());
    }
}
