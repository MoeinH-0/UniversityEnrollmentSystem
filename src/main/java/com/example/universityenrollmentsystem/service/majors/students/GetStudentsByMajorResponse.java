package com.example.universityenrollmentsystem.service.majors.students;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import java.util.List;

public record GetStudentsByMajorResponse(
        Long majorId,
        String majorName,
        List<MajorStudentSummaryDto> students
) {
    public record MajorStudentSummaryDto(
            Long studentId,
            String fullName,
            String studentNumber,
            EnrollmentPlanType planType,
            StudentStatus status
    ) {}
}
