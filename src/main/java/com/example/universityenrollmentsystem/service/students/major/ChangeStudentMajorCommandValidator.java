package com.example.universityenrollmentsystem.service.students.major;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class ChangeStudentMajorCommandValidator {
    public void validate(ChangeStudentMajorCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.studentId() == null)
            throw new CommandValidationException("StudentId cannot be null");

        if (command.newMajorId() == null)
            throw new CommandValidationException("NewMajorId cannot be null");
    }
}
