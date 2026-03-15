package com.educore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StartRegisterRequest {

    @NotBlank(message = "الاسم الأول مطلوب")
    private String firstName;

    @NotBlank(message = "رقم هاتف الطالب  مطلوب")
    @Pattern(regexp = "^01[0-9]{9}$", message = "رقم الهاتف غير صحيح")
    private String phone;

    @NotBlank(message = "رقم ولي الأمر مطلوب")
    @Pattern(regexp = "^01[0-9]{9}$", message = "رقم ولي الأمر غير صحيح")
    private String parentPhone;
//    private String fingerprint;


}