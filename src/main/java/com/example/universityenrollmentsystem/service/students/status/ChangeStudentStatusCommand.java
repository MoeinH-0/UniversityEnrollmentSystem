package com.example.universityenrollmentsystem.service.students.status;

import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;

public record ChangeStudentStatusCommand(Long studentId, StudentStatus newStatus) {}
