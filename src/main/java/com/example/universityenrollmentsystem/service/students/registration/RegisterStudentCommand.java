package com.example.universityenrollmentsystem.service.students.registration;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;

public record RegisterStudentCommand(
        String fullName,
        String studentNumber,
        Long majorId,
        EnrollmentPlanType planType
) {}
