package com.example.universityenrollmentsystem.service.courses.details;

import java.util.List;

public record GetCourseDetailsResponse(
        Long courseId,
        String name,
        String courseCode,
        int credits,
        boolean isDeleted,
        List<PrerequisiteDto> prerequisites,
        List<String> majors
) {
    public record PrerequisiteDto(Long id, String name, String courseCode, int credits) {}
}
