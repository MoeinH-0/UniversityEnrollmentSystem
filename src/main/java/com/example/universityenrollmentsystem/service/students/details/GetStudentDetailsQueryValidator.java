package com.example.universityenrollmentsystem.service.students.details;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetStudentDetailsQueryValidator {
    public void validate(GetStudentDetailsQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.studentId() == null)
            throw new CommandValidationException("StudentId cannot be null");
    }
}
