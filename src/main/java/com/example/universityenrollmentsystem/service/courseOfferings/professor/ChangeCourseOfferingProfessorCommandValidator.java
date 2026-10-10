package com.example.universityenrollmentsystem.service.courseOfferings.professor;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class ChangeCourseOfferingProfessorCommandValidator {
    public void validate(ChangeCourseOfferingProfessorCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.courseOfferingId() == null)
            throw new CommandValidationException("CourseOfferingId cannot be null");

        if (command.newProfessorName() == null || command.newProfessorName().isBlank())
            throw new CommandValidationException("NewProfessorName cannot be null or empty");
    }
}
