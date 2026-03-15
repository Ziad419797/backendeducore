package com.educore.unit;

import com.educore.auth.GlobalResponse;
import com.educore.dtocourse.request.CreateSessionRequest;
import com.educore.dtocourse.request.UpdateSessionRequest;
import com.educore.dtocourse.response.SessionResponse;
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
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sessions", description = "Session management APIs")
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "Create new session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody CreateSessionRequest request) {

        log.info("POST /api/sessions");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.createSession(request));
    }

    @Operation(summary = "Update session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session updated successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SessionResponse> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSessionRequest request) {

        log.info("PUT /api/sessions/{}", id);

        return ResponseEntity.ok(sessionService.updateSession(id, request));
    }

    @Operation(summary = "Delete session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {

        log.info("DELETE /api/sessions/{}", id);

        sessionService.deleteSession(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get session by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session found"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSessionById(@PathVariable Long id) {

        log.info("GET /api/sessions/{}", id);

        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @Operation(summary = "Get all sessions with pagination & sorting")
    @GetMapping
    public ResponseEntity<Page<SessionResponse>> getAllSessions(Pageable pageable) {

        log.info("GET /api/sessions");

        return ResponseEntity.ok(sessionService.getAllSessions(pageable));
    }

    @Operation(summary = "Get sessions by course with pagination & sorting")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<SessionResponse>> getSessionsByCourse(
            @PathVariable Long courseId,
            Pageable pageable) {

        log.info("GET /api/sessions/course/{}", courseId);

        return ResponseEntity.ok(
                sessionService.getSessionsByCourse(courseId, pageable)
        );
    }
    @Operation(summary = "Toggle session activation")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<GlobalResponse<Void>> toggleStatus(@PathVariable Long id) {
        sessionService.toggleSessionStatus(id);
        return ResponseEntity.ok(GlobalResponse.<Void>builder()
                .success(true)
                .message("تم تغيير حالة الجلسة بنجاح")
                .build());
    }
}
