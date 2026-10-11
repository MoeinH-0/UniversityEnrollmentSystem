package com.example.universityenrollmentsystem.service.students.transcript;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetStudentTranscriptQueryValidator {
    public void validate(GetStudentTranscriptQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.studentId() == null)
            throw new CommandValidationException("StudentId cannot be null");
    }
}
