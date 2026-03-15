package com.educore.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CompleteRegisterRequest {

    // ================= Authentication =================

    @NotBlank(message = "رقم الهاتف مطلوب")
    @Pattern(regexp = "^01[0-9]{9}$", message = "رقم الهاتف غير صحيح")
    private String phone;

    @NotBlank(message = "رقم ولي الأمر مطلوب")
    @Pattern(regexp = "^01[0-9]{9}$", message = "رقم ولي الأمر غير صحيح")
    private String parentPhone;

    @NotNull(message = "رمز التحقق مطلوب")
    @Min(value = 100000, message = "رمز التحقق غير صحيح")
    @Max(value = 999999, message = "رمز التحقق غير صحيح")
    private Integer otp;

    @NotBlank(message = "كلمة المرور مطلوبة")
    @Size(min = 6, message = "كلمة المرور يجب أن تكون 6 أحرف على الأقل")
    private String password;

    @NotBlank(message = "تأكيد كلمة المرور مطلوب")
    private String confirmPassword;

    // ================= Personal Info =================

    @NotBlank(message = "الاسم الأول مطلوب")
    private String firstName;

    private String secondName;
    private String thirdName;
    private String fourthName;

    // ================= Academic Info =================

    @NotBlank(message = "الصف الدراسي مطلوب")
    private String grade;

    @NotBlank(message = "المحافظة مطلوبة")
    private String governorate;

    private String area;
    private String schoolName;
    private String educationDepartment;

    // ================= Study Type =================

    @NotNull(message = "نوع الدراسة مطلوب (أونلاين أو مركز)")
    private Boolean online;

    private String centerName; // ⬅️ اتغير الاسم علشان يطابق الـ Service

    // ================= Documents =================

    private String profileImageUrl;
    private String identityDocumentUrl;
//    private String fingerprint;


}
