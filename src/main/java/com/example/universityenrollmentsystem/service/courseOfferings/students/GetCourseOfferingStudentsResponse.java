package com.example.universityenrollmentsystem.service.courseOfferings.students;

import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import java.util.List;

public record GetCourseOfferingStudentsResponse(Long courseOfferingId,
                                                List<CourseOfferingStudentDto> students) {
    public record CourseOfferingStudentDto(
        Long studentId, 
        String fullName, 
        String studentNumber, 
        Double grade, 
        CourseStatus courseStatus
    ) {}
}
