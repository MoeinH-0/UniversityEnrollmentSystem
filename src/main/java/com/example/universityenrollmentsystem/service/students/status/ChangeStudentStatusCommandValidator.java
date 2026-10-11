package com.example.universityenrollmentsystem.service.students.status;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class ChangeStudentStatusCommandValidator {
    public void validate(ChangeStudentStatusCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.studentId() == null)
            throw new CommandValidationException("StudentId cannot be null");

        if (command.newStatus() == null)
            throw new CommandValidationException("NewStatus cannot be null");
    }
}
