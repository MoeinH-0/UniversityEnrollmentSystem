package com.example.universityenrollmentsystem.service.courseOfferings.create;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class CreateCourseOfferingCommandValidator {
    public void validate(CreateCourseOfferingCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.courseId() == null)
            throw new CommandValidationException("CourseId cannot be null");

        if (command.semesterId() == null)
            throw new CommandValidationException("SemesterId cannot be null");

        if (command.professorName() == null || command.professorName().isBlank())
            throw new CommandValidationException("ProfessorName cannot be null or empty");

        if (command.section() == null || command.section().isBlank())
            throw new CommandValidationException("Section cannot be null or empty");

        if (command.capacity() <= 0)
            throw new CommandValidationException("Capacity must be greater than zero");
    }
}
