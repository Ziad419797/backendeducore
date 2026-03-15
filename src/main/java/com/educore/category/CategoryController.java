package com.educore.category;

import com.educore.auth.GlobalResponse;
import com.educore.dtocourse.request.CategoryCreateRequest;
import com.educore.dtocourse.request.CategoryUpdateRequest;
import com.educore.dtocourse.response.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create new category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Level not found")
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    @Operation(summary = "Update category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {

        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "Delete category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get category by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {

        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Get categories by level with pagination")
    @GetMapping("/level/{levelId}")
    public ResponseEntity<Page<CategoryResponse>> getCategoriesByLevel(
            @PathVariable Long levelId,
            Pageable pageable) {

        return ResponseEntity.ok(
                categoryService.getCategoriesByLevel(levelId, pageable)
        );
    }

    @Operation(summary = "Get all categories with pagination")
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(Pageable pageable) {

        return ResponseEntity.ok(
                categoryService.getAllCategories(pageable)
        );
    }

    // ================= active =================
// CategoryController.java

    @Operation(summary = "Toggle category activation", description = "إظهار أو إخفاء القسم بالكامل وما يحتويه من كورسات")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status toggled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<GlobalResponse<Void>> toggleStatus(@PathVariable Long id) {
        log.info("PATCH /api/categories/{}/toggle-status", id);

        categoryService.toggleCategoryStatus(id);

        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم تحديث حالة القسم بنجاح")
                .build());
    }
}
