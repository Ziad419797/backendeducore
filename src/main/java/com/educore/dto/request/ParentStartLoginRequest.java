package com.educore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class ParentStartLoginRequest {
    @NotBlank(message = "رقم ولي الأمر مطلوب")
    @Pattern(regexp = "^01[0-9]{9}$", message = "رقم الطالب غير صحيح")
    private String  ParentPhone;
}
