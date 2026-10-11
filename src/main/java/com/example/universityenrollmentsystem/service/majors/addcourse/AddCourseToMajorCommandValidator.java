package com.example.universityenrollmentsystem.service.majors.addcourse;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class AddCourseToMajorCommandValidator {
    public void validate(AddCourseToMajorCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.majorId() == null)
            throw new CommandValidationException("MajorId cannot be null");

        if (command.courseId() == null)
            throw new CommandValidationException("CourseId cannot be null");
    }
}
