package com.educore.student;

import com.educore.dto.mapper.ApplicationInfoMapper;
import com.educore.dto.request.CompleteRegisterRequest;
import com.educore.dto.request.ResendOtpRequest;
import com.educore.dto.request.StartRegisterRequest;
import com.educore.dto.response.CompleteRegisterResponse;
import com.educore.dto.response.ResendOtpResponse;
import com.educore.dto.response.StartRegisterResponse;
import com.educore.exception.ResourceAlreadyExistsException;
import com.educore.location.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/student/register")
@RequiredArgsConstructor
public class StudentRegistrationController {

    private final LocationService locationService;
    private final StudentRegistrationService registrationService;
    private final ApplicationInfoMapper applicationInfoMapper;

    /* =========================
       Location Endpoints
       ========================= */

    @GetMapping("/governorates")
    public ResponseEntity<?> getGovernorates() {
        try {
            List<String> governorates = locationService.getGovernorates();
            return ResponseEntity.ok(governorates);
        } catch (Exception e) {
            log.error("Error getting governorates: {}", e.getMessage());
            return buildErrorResponse("فشل في جلب المحافظات", e.getMessage());
        }
    }

    @GetMapping("/areas/{governorate}")
    public ResponseEntity<?> getAreas(@PathVariable String governorate) {
        try {
            List<String> areas = locationService.getAreas(governorate);

            if (areas == null || areas.isEmpty()) {
                return buildNotFoundResponse("لا توجد مناطق لهذه المحافظة", governorate);
            }

            return ResponseEntity.ok(areas);
        } catch (Exception e) {
            log.error("Error getting areas for governorate {}: {}", governorate, e.getMessage());
            return buildErrorResponse("فشل في جلب المناطق", e.getMessage());
        }
    }

    /* =========================
       Registration Endpoints
       ========================= */

    @PostMapping("/start")
    public ResponseEntity<?> startRegistration(@Valid @RequestBody StartRegisterRequest request) {
        try {
            log.info("Starting registration for phone: {}", request.getPhone());
            StartRegisterResponse response = registrationService.startRegistration(request);
            return buildSuccessResponse(response, "VERIFY_OTP");
        } catch (ResourceAlreadyExistsException ex) {
            log.warn("Phone already exists: {}", request.getPhone());
            return buildConflictResponse(ex.getMessage(), request.getPhone());
        } catch (Exception ex) {
            log.error("Error starting registration for {}: {}", request.getPhone(), ex.getMessage());
            return buildErrorResponse("فشل في بدء التسجيل", ex.getMessage(), request.getPhone());
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeRegistration(
            @Valid @RequestBody CompleteRegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            log.info("Completing registration for phone: {}", request.getPhone());
            CompleteRegisterResponse response = registrationService.completeRegistration(request, httpRequest);
            return buildCompleteSuccessResponse(response);
        } catch (IllegalArgumentException ex) {
            log.warn("Validation error for {}: {}", request.getPhone(), ex.getMessage());
            return buildBadRequestResponse(ex.getMessage(), request.getPhone());
        } catch (ResourceAlreadyExistsException ex) {
            log.warn("Phone already exists during completion: {}", request.getPhone());
            return buildConflictResponse(ex.getMessage(), request.getPhone());
        } catch (Exception ex) {
            log.error("Error completing registration for {}: {}", request.getPhone(), ex.getMessage());
            return buildErrorResponse("فشل في إتمام التسجيل", ex.getMessage(), request.getPhone());
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        try {
            log.info("Resending OTP for phone: {}", request.getPhone());
            ResendOtpResponse response = registrationService.resendOtp(request.getPhone());
            return buildSimpleSuccessResponse(response.getMessage(), request.getPhone());
        } catch (ResourceAlreadyExistsException ex) {
            log.warn("Phone already exists: {}", request.getPhone());
            return buildConflictResponse(ex.getMessage(), request.getPhone());
        } catch (Exception ex) {
            log.warn("Error resending OTP for {}: {}", request.getPhone(), ex.getMessage());
            return buildErrorResponse("فشل في إعادة إرسال رمز التحقق", ex.getMessage(), request.getPhone());
        }
    }

    @GetMapping("/check-phone/{phone}")
    public ResponseEntity<?> checkPhone(@PathVariable String phone) {
        try {
            var response = registrationService.checkPhoneStatus(phone);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Phone check error: {}", e.getMessage());
            return buildPhoneCheckErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/application-info")
    public ResponseEntity<?> getApplicationInfo() {
        try {
            return ResponseEntity.ok(applicationInfoMapper.getApplicationInfo());
        } catch (Exception e) {
            log.error("Error getting application info: {}", e.getMessage());
            return buildErrorResponse("فشل في جلب معلومات التقديم", e.getMessage());
        }
    }

    /* =========================
       Helper Methods for Building Responses
       ========================= */

    private ResponseEntity<?> buildSuccessResponse(StartRegisterResponse response, String action) {
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", response.getMessage());
        successResponse.put("phone", response.getPhone());
        successResponse.put("action", action);
        return ResponseEntity.ok(successResponse);
    }

    private ResponseEntity<?> buildCompleteSuccessResponse(CompleteRegisterResponse response) {
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", response.getMessage());
        successResponse.put("phone", response.getPhone());
        successResponse.put("token", response.getToken());
        successResponse.put("studentCode", response.getStudentCode());
        successResponse.put("status", "PENDING");
        successResponse.put("redirectTo", "/pending");
        successResponse.put("supportWhatsApp", "+201234567890");
        return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
    }

    private ResponseEntity<?> buildSimpleSuccessResponse(String message, String phone) {
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", message);
        successResponse.put("phone", phone);
        return ResponseEntity.ok(successResponse);
    }

    private ResponseEntity<?> buildConflictResponse(String error, String phone) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", error);
        errorResponse.put("phone", phone);
        errorResponse.put("action", "LOGIN");
        errorResponse.put("loginUrl", "/api/auth/login");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    private ResponseEntity<?> buildBadRequestResponse(String message, String phone) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "خطأ في البيانات");
        errorResponse.put("message", message);
        errorResponse.put("phone", phone);
        errorResponse.put("action", "FIX_DATA");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private ResponseEntity<?> buildErrorResponse(String error, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ResponseEntity<?> buildErrorResponse(String error, String message, String phone) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("phone", phone);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ResponseEntity<?> buildNotFoundResponse(String message, String governorate) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("governorate", governorate);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    private ResponseEntity<?> buildPhoneCheckErrorResponse(String error) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("exists", false);
        errorResponse.put("message", "حدث خطأ في التحقق");
        errorResponse.put("error", error);
        return ResponseEntity.ok(errorResponse);
    }
}