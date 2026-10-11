package com.example.universityenrollmentsystem.service.courses.credits;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class UpdateCourseCreditsCommandValidator {
    public void validate(UpdateCourseCreditsCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.courseId() == null)
            throw new CommandValidationException("CourseId cannot be null");

        if (command.newCredits() <= 0)
            throw new CommandValidationException("Credits must be greater than zero");
    }
}
