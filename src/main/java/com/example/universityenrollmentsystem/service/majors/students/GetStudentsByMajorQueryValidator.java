package com.example.universityenrollmentsystem.service.majors.students;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetStudentsByMajorQueryValidator {
    public void validate(GetStudentsByMajorQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.majorId() == null)
            throw new CommandValidationException("MajorId cannot be null");
    }
}
