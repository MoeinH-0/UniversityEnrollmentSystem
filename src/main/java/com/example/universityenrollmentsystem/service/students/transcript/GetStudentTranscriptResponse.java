package com.example.universityenrollmentsystem.service.students.transcript;

import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import java.util.List;

public record GetStudentTranscriptResponse(
        Long studentId,
        String fullName,
        String studentNumber,
        String majorName,
        double gpa,
        int totalPassedCredits,
        List<SemesterTranscriptDto> semesters
) {
    public record SemesterTranscriptDto(
            Long semesterId,
            String semesterTitle,
            double semesterGpa,
            int semesterCredits,
            List<CourseGradeDto> courses
    ) {}

    public record CourseGradeDto(
            Long courseId,
            String courseName,
            String courseCode,
            int credits,
            Double grade,
            CourseStatus status
    ) {}
}
