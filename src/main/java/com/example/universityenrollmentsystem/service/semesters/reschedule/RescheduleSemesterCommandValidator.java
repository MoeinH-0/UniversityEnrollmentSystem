package com.example.universityenrollmentsystem.service.semesters.reschedule;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class RescheduleSemesterCommandValidator {
    public void validate(RescheduleSemesterCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.semesterId() == null)
            throw new CommandValidationException("SemesterId cannot be null");

        if (command.newStartDate() == null)
            throw new CommandValidationException("NewStartDate cannot be null");

        if (command.newEndDate() == null)
            throw new CommandValidationException("NewEndDate cannot be null");

        if (command.newEndDate().isBefore(command.newStartDate()))
            throw new CommandValidationException("NewEndDate cannot be before NewStartDate");
    }
}
