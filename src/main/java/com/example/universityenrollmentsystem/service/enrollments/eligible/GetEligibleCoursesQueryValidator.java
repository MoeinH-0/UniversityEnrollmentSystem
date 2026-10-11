package com.example.universityenrollmentsystem.service.enrollments.eligible;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetEligibleCoursesQueryValidator {

    public void validate(GetEligibleCoursesQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.studentId() == null)
            throw new CommandValidationException("StudentId cannot be null");

        if (query.semesterId() == null)
            throw new CommandValidationException("SemesterId cannot be null");
    }
}
