package com.example.universityenrollmentsystem.service.enrollments.submit;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class SubmitEnrollmentCommandValidator {

    public void validate(SubmitEnrollmentCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.studentId() == null)
            throw new CommandValidationException("StudentId cannot be null");

        if (command.courseOfferingId() == null)
            throw new CommandValidationException("CourseOfferingId cannot be null");
    }
}
