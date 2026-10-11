package com.example.universityenrollmentsystem.service.enrollments.drop;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class DropCourseOfferingCommandValidator {

    public void validate(DropCourseOfferingCommand command){
        if (command == null)
            throw new CommandValidationException("command cannot be null");

        if (command.courseOfferingId() == null)
            throw new CommandValidationException("course offering id cannot be null");

        if (command.studentId() == null)
            throw new CommandValidationException("student id cannot be null");
    }
}

