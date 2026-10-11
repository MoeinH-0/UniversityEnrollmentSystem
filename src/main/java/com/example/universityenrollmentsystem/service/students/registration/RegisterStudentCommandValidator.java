package com.example.universityenrollmentsystem.service.students.registration;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class RegisterStudentCommandValidator {
    public void validate(RegisterStudentCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.fullName() == null || command.fullName().isBlank())
            throw new CommandValidationException("FullName cannot be null or empty");

        if (command.studentNumber() == null || command.studentNumber().isBlank())
            throw new CommandValidationException("StudentNumber cannot be null or empty");

        if (command.majorId() == null)
            throw new CommandValidationException("MajorId cannot be null");

        if (command.planType() == null)
            throw new CommandValidationException("PlanType cannot be null");
    }
}
