package com.educore.dto.mapper;

import com.educore.dto.request.CompleteRegisterRequest;
import com.educore.dto.response.StudentResponse;
import com.educore.student.Student;
import com.educore.student.StudentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", imports = {StudentStatus.class})
public interface StudentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // هنشفره في الـ Service
    @Mapping(target = "studentCode", ignore = true) // هيتولد في PrePersist
    @Mapping(target = "status", expression = "java(StudentStatus.PENDING)")
    @Mapping(target = "enabled", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "devicesCount", constant = "0")
    @Mapping(target = "loginCount", constant = "0")
    @Mapping(target = "logoutCount", constant = "0")
    @Mapping(target = "activeDeviceId", ignore = true)
    @Mapping(target = "activeSessionId", ignore = true)
    @Mapping(target = "lastDeviceFingerprint", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLogoutAt", ignore = true)
    @Mapping(target = "lastActivityAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdBy", constant = "SYSTEM")
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "rejectedBy", ignore = true)
    @Mapping(target = "online", source = "online", defaultValue = "false")
    Student toEntity(CompleteRegisterRequest request);

    @Mapping(target = "fullName", expression = "java(student.getFullName())")
    @Mapping(target = "shortName", expression = "java(student.getShortName())")
    @Mapping(target = "studyType", expression = "java(student.getStudyType())")
    @Mapping(target = "studyLocation", expression = "java(student.getStudyLocation())")
    @Mapping(target = "hasActiveSession", expression = "java(student.hasActiveSession())")
    @Mapping(target = "isPending", expression = "java(student.isPending())")
    @Mapping(target = "isActive", expression = "java(student.isActive())")
    @Mapping(target = "isRejected", expression = "java(student.isRejected())")
    StudentResponse toResponse(Student student);
}