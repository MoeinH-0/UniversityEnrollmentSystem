package com.example.universityenrollmentsystem.service.courseOfferings.exam;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class RescheduleCourseOfferingExamCommandValidator {
    public void validate(RescheduleCourseOfferingExamCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.courseOfferingId() == null)
            throw new CommandValidationException("CourseOfferingId cannot be null");

        if (command.newExamDate() == null)
            throw new CommandValidationException("NewExamDate cannot be null");
    }
}
