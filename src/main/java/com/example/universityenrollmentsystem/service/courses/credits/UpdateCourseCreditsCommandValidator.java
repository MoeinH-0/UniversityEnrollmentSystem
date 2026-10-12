package com.example.universityenrollmentsystem.service.courses.credits;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class UpdateCourseCreditsCommandValidator {
    public void validate(UpdateCourseCreditsCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.courseId() == null)
            throw new CommandValidationException("CourseId cannot be null");

        if (command.newCredits() < Course.MIN_CREDITS || command.newCredits() > Course.MAX_CREDITS)
            throw new CommandValidationException(String.format("Credits must be between %d and %d", Course.MIN_CREDITS, Course.MAX_CREDITS));
    }
}
