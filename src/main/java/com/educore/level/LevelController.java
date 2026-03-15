package com.educore.level;

import com.educore.dtocourse.request.LevelCreateRequest;
import com.educore.dtocourse.request.LevelUpdateRequest;
import com.educore.dtocourse.response.LevelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/levels")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Levels", description = "إدارة الصفوف الدراسية (أولى ثانوي - تانية ثانوي...)")
public class LevelController {

    private final LevelService levelService;

    @Operation(
            summary = "إنشاء صف دراسي",
            description = "يستخدم لإنشاء صف دراسي جديد (مثال: أولى ثانوي)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "تم إنشاء الصف بنجاح"),
            @ApiResponse(responseCode = "409", description = "الصف موجود بالفعل"),
            @ApiResponse(responseCode = "400", description = "بيانات غير صحيحة")
    })
    @PostMapping
    public ResponseEntity<LevelResponse> createLevel(@Validated @RequestBody LevelCreateRequest request) {
        log.info("API /api/levels [POST] called");
        LevelResponse response = levelService.createLevel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }





    @PutMapping("/{id}")
    public ResponseEntity<LevelResponse> updateLevel(
            @PathVariable Long id,
            @Validated @RequestBody LevelUpdateRequest request
    ) {
        log.info("API /api/levels/{} [PUT] called", id);
        LevelResponse response = levelService.updateLevel(id, request);
        return ResponseEntity.ok(response);
    }





    @Operation(
            summary = "حذف صف دراسي",
            description = "حذف صف دراسي (لو مفيش وحدات مرتبطة)"
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteLevel(@PathVariable Long id) {
        log.info("API /api/levels/{} [DELETE] called", id);
        levelService.deleteLevel(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "جلب صف دراسي",
            description = "إرجاع تفاصيل صف دراسي واحد باستخدام ID"
    )

    @GetMapping("/{id}")
    public ResponseEntity<LevelResponse> getLevelById(@PathVariable Long id) {
        log.info("API /api/levels/{} [GET] called", id);
        LevelResponse response = levelService.getLevelById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "جلب كل الصفوف",
            description = "إرجاع كل الصفوف الدراسية مع الوحدات"
    )
    @GetMapping
    public ResponseEntity<List<LevelResponse>> getAllLevels() {
        log.info("API /api/levels [GET] called");
        List<LevelResponse> levels = levelService.getAllLevels();
        return ResponseEntity.ok(levels);
    }
}

