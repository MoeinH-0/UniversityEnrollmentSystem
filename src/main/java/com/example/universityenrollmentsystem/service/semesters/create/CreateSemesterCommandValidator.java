package com.example.universityenrollmentsystem.service.semesters.create;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreateSemesterCommandValidator {
    public void validate(CreateSemesterCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.title() == null || command.title().isBlank())
            throw new CommandValidationException("Title cannot be null or empty");

        if (command.startDate() == null)
            throw new CommandValidationException("StartDate cannot be null");

        if (command.endDate() == null)
            throw new CommandValidationException("EndDate cannot be null");

        if (command.endDate().isBefore(command.startDate()))
            throw new CommandValidationException("EndDate cannot be before StartDate");
    }
}
