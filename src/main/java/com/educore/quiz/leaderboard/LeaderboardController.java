package com.educore.quiz.leaderboard;

import com.educore.auth.GlobalResponse;
import com.educore.dtocourse.response.LeaderboardResponse;
import com.educore.security.JwtUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "لوحة الشرف وأفضل الطلاب")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "أفضل الطلاب في اختبار محدد")
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<GlobalResponse<LeaderboardResponse>> getQuizLeaderboard(
            @PathVariable @Min(1) Long quizId      )
    {
        LeaderboardResponse response = leaderboardService.getQuizLeaderboard(quizId);
        return ResponseEntity.ok(GlobalResponse.success(response, "تم جلب لوحة الشرف بنجاح"));
    }



    @Operation(summary = "أفضل الطلاب في كورس محدد")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<GlobalResponse<LeaderboardResponse>> getCourseLeaderboard(
            @PathVariable Long courseId
    ) {
        LeaderboardResponse response = leaderboardService.getCourseLeaderboard(courseId);
        return ResponseEntity.ok(GlobalResponse.success(response, "تم جلب لوحة الشرف بنجاح"));
    }



    @Operation(
            summary = "أفضل الطلاب في النظام",
            description = "يجلب قائمة مرتبة بأعلى الطلاب تحصيلاً على مستوى المنصة مع دعم التقسيم (Pagination)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "تمت العملية بنجاح"),
            @ApiResponse(responseCode = "401", description = "غير مصرح (التوكن منتهي أو غير موجود)")
    })
    @GetMapping("/global")
    public ResponseEntity<GlobalResponse<LeaderboardResponse>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {
        LeaderboardResponse response = leaderboardService.getGlobalLeaderboard(page, size);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))  // ✅ Cache لمدة 5 دقائق
                .body(GlobalResponse.success(response, "تم جلب لوحة الشرف بنجاح"));
    }



    @Operation(summary = "ترتيبي في اختبار محدد")
    @GetMapping("/quiz/{quizId}/my-rank")
    public ResponseEntity<GlobalResponse<Integer>> getMyRankInQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestHeader("X-Device-Id") String deviceId// 👈 سواجر هيظهرلها خانة هنا


    ) {
        Integer rank = leaderboardService.getMyRankInQuiz(quizId, principal);
        return ResponseEntity.ok(GlobalResponse.success(rank, "تم جلب ترتيبك بنجاح"));
    }
}