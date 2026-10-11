package com.example.universityenrollmentsystem.service.students.contact;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class UpdateStudentContactInfoCommandValidator {
    public void validate(UpdateStudentContactInfoCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.studentId() == null)
            throw new CommandValidationException("StudentId cannot be null");
    }
}
