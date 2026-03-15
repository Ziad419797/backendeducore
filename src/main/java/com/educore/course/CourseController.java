package com.educore.course;

import com.educore.auth.GlobalResponse;
import com.educore.dtocourse.request.CreateCourseRequest;
import com.educore.dtocourse.request.UpdateCourseRequest;
import com.educore.dtocourse.response.CourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Courses", description = "Course management APIs")
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Create new course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Course created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {

        log.info("POST /api/courses");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.createCourse(request));
    }

    @Operation(summary = "Update existing course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {

        log.info("PUT /api/courses/{}", id);

        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @Operation(summary = "Delete course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Course deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {

        log.info("DELETE /api/courses/{}", id);

        courseService.deleteCourse(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get course by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course found"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {

        log.info("GET /api/courses/{}", id);

        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @Operation(summary = "Get all courses with pagination & sorting")
    @GetMapping
    public ResponseEntity<Page<CourseResponse>> getAllCourses(Pageable pageable) {

        log.info("GET /api/courses");

        return ResponseEntity.ok(courseService.getAllCourses(pageable));
    }

    @Operation(summary = "Get courses by category with pagination & sorting")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<CourseResponse>> getCoursesByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {

        log.info("GET /api/courses/category/{}", categoryId);

        return ResponseEntity.ok(
                courseService.getCoursesByCategory(categoryId, pageable)
        );
    }


    // ================= active =================

    // TeacherCourseController.java

    @Operation(summary = "Toggle course activation status", description = "إغلاق أو فتح الكورس للطلاب")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status toggled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<GlobalResponse<Void>> toggleStatus(@PathVariable Long id) {

        log.info("PATCH /api/courses/{}/toggle-status", id);

        courseService.toggleCourseStatus(id);

        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم تغيير حالة النشاط بنجاح")
                .build());
    }
}
