package com.example.universityenrollmentsystem.service.majors.catalog;

import java.util.List;

public record GetMajorCatalogResponse(
        Long majorId,
        String majorName,
        List<MajorCatalogCourseDto> courses
) {
    public record MajorCatalogCourseDto(
            Long courseId,
            String name,
            String courseCode,
            int credits
    ) {}
}
