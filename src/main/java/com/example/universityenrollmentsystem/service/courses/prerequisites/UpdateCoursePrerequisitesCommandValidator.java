package com.example.universityenrollmentsystem.service.courses.prerequisites;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class UpdateCoursePrerequisitesCommandValidator {
    public void validate(UpdateCoursePrerequisitesCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.courseId() == null)
            throw new CommandValidationException("CourseId cannot be null");

        if (command.prerequisiteIds() != null && command.prerequisiteIds().stream().distinct().count()
                != command.prerequisiteIds().size())
            throw new CommandValidationException("Prerequisite IDs cannot contain duplicates");
    }
}
