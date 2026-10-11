package com.example.universityenrollmentsystem.service.courses.statistics;

import java.util.List;

public record GetCourseStatisticsResponse(
        OverallCourseStatistics overall,
        List<MajorCourseStatistics> byMajor
) {
    public record OverallCourseStatistics(
            Long courseId,
            String courseName,
            String courseCode,
            int totalOfferings,
            int totalStudentsPassed,
            int totalStudentsFailed,
            int totalStudentsInProgress,
            double averageGrade
    ) {}

    public record MajorCourseStatistics(
            Long majorId,
            String majorName,
            int totalStudentsPassed,
            int totalStudentsFailed,
            int totalStudentsInProgress,
            double averageGrade
    ) {}
}
