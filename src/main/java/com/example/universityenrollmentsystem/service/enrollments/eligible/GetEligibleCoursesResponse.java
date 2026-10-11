package com.example.universityenrollmentsystem.service.enrollments.eligible;

import java.time.LocalDate;
import java.util.List;

public record GetEligibleCoursesResponse(
        Long studentId,
        Long semesterId,
        List<EligibleCourseOfferingDto> eligibleOfferings
) {
    public record EligibleCourseOfferingDto(
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
