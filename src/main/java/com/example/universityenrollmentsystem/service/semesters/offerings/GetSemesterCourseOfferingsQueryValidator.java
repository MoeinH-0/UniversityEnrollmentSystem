package com.example.universityenrollmentsystem.service.semesters.offerings;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetSemesterCourseOfferingsQueryValidator {
    public void validate(GetSemesterCourseOfferingsQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.semesterId() == null)
            throw new CommandValidationException("SemesterId cannot be null");
    }
}
