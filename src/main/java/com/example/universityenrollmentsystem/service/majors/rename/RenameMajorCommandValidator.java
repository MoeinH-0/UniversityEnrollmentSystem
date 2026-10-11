package com.example.universityenrollmentsystem.service.majors.rename;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class RenameMajorCommandValidator {
    public void validate(RenameMajorCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.majorId() == null)
            throw new CommandValidationException("MajorId cannot be null");

        if (command.newName() == null || command.newName().isBlank())
            throw new CommandValidationException("NewName cannot be null or empty");
    }
}
