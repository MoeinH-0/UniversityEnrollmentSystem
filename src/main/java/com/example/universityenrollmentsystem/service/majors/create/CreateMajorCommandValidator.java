package com.example.universityenrollmentsystem.service.majors.create;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreateMajorCommandValidator {
    public void validate(CreateMajorCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.name() == null || command.name().isBlank())
            throw new CommandValidationException("Name cannot be null or empty");
    }
}
