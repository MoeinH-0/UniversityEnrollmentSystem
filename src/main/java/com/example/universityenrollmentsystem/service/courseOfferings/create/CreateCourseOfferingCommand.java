package com.example.universityenrollmentsystem.service.courseOfferings.create;

import java.time.LocalDate;

public record CreateCourseOfferingCommand(
    Long courseId,
    Long semesterId,
    String professorName,
    String section,
    boolean guestAllowed,
    int capacity,
    String classTime,
    LocalDate examDate
) {}
