package com.educore.dto.mapper;

import com.educore.dto.response.TeacherAuthResponse;
import com.educore.teacher.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

    @Mapping(target = "accessToken", ignore = true) // بنضيفه يدويا في السيرفيس
    @Mapping(target = "refreshToken", ignore = true) // بنضيفه يدويا في السيرفيس
    @Mapping(target = "role", constant = "TEACHER")
    @Mapping(target = "message", ignore = true)
    TeacherAuthResponse toAuthResponse(Teacher teacher);
}