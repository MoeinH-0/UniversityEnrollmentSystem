package com.example.universityenrollmentsystem.service.students.details;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;

public record GetStudentDetailsResponse(
        Long studentId,
        String fullName,
        String studentNumber,
        Long majorId,
        String majorName,
        EnrollmentPlanType planType,
        StudentStatus status,
        String email,
        String phoneNumber
) {}
