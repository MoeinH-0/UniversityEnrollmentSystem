package com.example.universityenrollmentsystem.service.semesters.offerings;

import java.time.LocalDate;
import java.util.List;

public record GetSemesterCourseOfferingsResponse(
        Long semesterId,
        String semesterTitle,
        List<SemesterCourseOfferingDto> offerings
) {
    public record SemesterCourseOfferingDto(
            Long offeringId,
            Long courseId,
            String courseName,
            String courseCode,
            int credits,
            String professorName,
            String section,
            int capacity,
            String classTime,
            LocalDate examDate,
            boolean guestAllowed
    ) {}
}
