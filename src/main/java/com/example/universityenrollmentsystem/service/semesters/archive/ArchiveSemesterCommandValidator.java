package com.example.universityenrollmentsystem.service.semesters.archive;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class ArchiveSemesterCommandValidator {
    public void validate(ArchiveSemesterCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.semesterId() == null)
            throw new CommandValidationException("SemesterId cannot be null");
    }
}
