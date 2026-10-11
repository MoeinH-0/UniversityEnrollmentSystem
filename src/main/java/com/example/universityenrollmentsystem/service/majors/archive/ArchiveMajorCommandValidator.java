package com.example.universityenrollmentsystem.service.majors.archive;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class ArchiveMajorCommandValidator {
    public void validate(ArchiveMajorCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.majorId() == null)
            throw new CommandValidationException("MajorId cannot be null");
    }
}
