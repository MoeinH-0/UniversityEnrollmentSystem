package com.example.universityenrollmentsystem.service.courses.create;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class CreateCourseCommandValidator {
    public void validate(CreateCourseCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.name() == null || command.name().isBlank())
            throw new CommandValidationException("Name cannot be null or empty");

        if (command.courseCode() == null || command.courseCode().isBlank())
            throw new CommandValidationException("CourseCode cannot be null or empty");

        if (command.credits() < Course.MIN_CREDITS || command.credits() > Course.MAX_CREDITS)
            throw new CommandValidationException(String.format("Credits must be between %d and %d", Course.MIN_CREDITS, Course.MAX_CREDITS));

        if (command.majorIds() == null || command.majorIds().isEmpty())
            throw new CommandValidationException("Course must be assigned to at least one major");
    }
}
