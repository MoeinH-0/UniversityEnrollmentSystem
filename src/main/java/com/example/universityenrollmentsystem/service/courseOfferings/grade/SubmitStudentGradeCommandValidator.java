package com.example.universityenrollmentsystem.service.courseOfferings.grade;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class SubmitStudentGradeCommandValidator {
    public void validate(SubmitStudentGradeCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.studentId() == null)
            throw new CommandValidationException("StudentId cannot be null");

        if (command.courseOfferingId() == null)
            throw new CommandValidationException("CourseOfferingId cannot be null");

        if (command.grade() == null)
            throw new CommandValidationException("Grade cannot be null");
    }
}
