package com.example.universityenrollmentsystem.service.semesters.details;

import java.time.LocalDate;

public record GetSemesterDetailsResponse(
        Long semesterId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        boolean isDeleted
) {}
