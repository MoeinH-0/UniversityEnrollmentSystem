package com.example.universityenrollmentsystem.service.courseOfferings.details;

import java.time.LocalDate;

public record GetCourseOfferingDetailsResponse(
        Long offeringId,
        Long courseId,
        String courseName,
        String courseCode,
        int credits,
        Long semesterId,
        String semesterTitle,
        String professorName,
        String section,
        boolean guestAllowed,
        int capacity,
        int enrolledCount,
        String classTime,
        LocalDate examDate,
        boolean isDeleted
) {}
