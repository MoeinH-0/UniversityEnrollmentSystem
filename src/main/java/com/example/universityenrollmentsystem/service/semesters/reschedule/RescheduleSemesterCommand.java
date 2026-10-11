package com.example.universityenrollmentsystem.service.semesters.reschedule;

import java.time.LocalDate;

public record RescheduleSemesterCommand(Long semesterId, LocalDate newStartDate, LocalDate newEndDate) {}
