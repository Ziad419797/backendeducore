package com.educore.dto.mapper;

import org.mapstruct.Mapper;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ApplicationInfoMapper {

    default Map<String, Object> getApplicationInfo() {
        return Map.of(
                "steps", getRegistrationSteps(),
                "requiredDocuments", getRequiredDocuments(),
                "supportInfo", getSupportInfo()
        );
    }

    default List<Map<String, String>> getRegistrationSteps() {
        return List.of(
                Map.of("step", "1", "title", "بدء التسجيل", "description", "إدخال رقم الهاتف والحصول على رمز التحقق"),
                Map.of("step", "2", "title", "التأكيد", "description", "إدخال رمز التحقق وإكمال البيانات"),
                Map.of("step", "3", "title", "المراجعة", "description", "سيتم مراجعة طلبك من قبل الإدارة")
        );
    }

    default List<String> getRequiredDocuments() {
        return List.of(
                "صورة شخصية",
                "صورة بطاقة الهوية/شهادة الميلاد",
                "شهادة المرحلة الدراسية الأخيرة"
        );
    }

    default Map<String, String> getSupportInfo() {
        return Map.of(
                "whatsapp", "+201234567890",
                "email", "support@educationplatform.com",
                "workingHours", "من 9 صباحاً إلى 5 مساءً"
        );
    }
}