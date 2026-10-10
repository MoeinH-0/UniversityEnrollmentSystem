package com.example.universityenrollmentsystem.service.courseOfferings.grade;

public record SubmitStudentGradeCommand(Long studentId, Long courseOfferingId, Double grade) {
}
