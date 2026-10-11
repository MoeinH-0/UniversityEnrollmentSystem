package com.example.universityenrollmentsystem.service.semesters.details;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetSemesterDetailsQueryValidator {
    public void validate(GetSemesterDetailsQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.semesterId() == null)
            throw new CommandValidationException("SemesterId cannot be null");
    }
}
