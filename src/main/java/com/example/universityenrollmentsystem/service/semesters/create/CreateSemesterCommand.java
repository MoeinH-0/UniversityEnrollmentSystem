package com.example.universityenrollmentsystem.service.semesters.create;

import java.time.LocalDate;

public record CreateSemesterCommand(String title, LocalDate startDate, LocalDate endDate) {}
