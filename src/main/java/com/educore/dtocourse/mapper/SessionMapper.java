package com.educore.dtocourse.mapper;

import com.educore.course.Course;
import com.educore.dtocourse.request.CreateSessionRequest;
import com.educore.dtocourse.request.UpdateSessionRequest;
import com.educore.dtocourse.response.SessionResponse;
import com.educore.unit.Session;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    Session toEntity(CreateSessionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateSessionRequest request, @MappingTarget Session session);

    @Mapping(target = "courseIds", expression = "java(mapCoursesToIds(session.getCourses()))")
    SessionResponse toResponse(Session session);

    default Set<Long> mapCoursesToIds(Set<Course> courses) {
        if (courses == null) return null;
        return courses.stream().map(Course::getId).collect(Collectors.toSet());
    }
}
