package com.example.universityenrollmentsystem.service.courses.create;

import java.util.List;

public record CreateCourseCommand(
    String name,
    String courseCode,
    int credits,
    List<Long> majorIds,
    List<Long> prerequisiteIds
) {}
