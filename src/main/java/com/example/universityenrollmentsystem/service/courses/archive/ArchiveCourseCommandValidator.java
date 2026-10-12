package com.example.universityenrollmentsystem.service.courses.archive;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class ArchiveCourseCommandValidator {
    public void validate(ArchiveCourseCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.courseId() == null)
            throw new CommandValidationException("CourseId cannot be null");
    }
}
