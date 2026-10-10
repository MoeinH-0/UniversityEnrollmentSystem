package com.example.universityenrollmentsystem.service.courseOfferings.exam;

import java.time.LocalDate;

public record RescheduleCourseOfferingExamCommand(Long courseOfferingId, LocalDate newExamDate) {
}
