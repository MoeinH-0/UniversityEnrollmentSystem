package com.example.universityenrollmentsystem.service.courseOfferings.capacity;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class ChangeCourseOfferingCapacityCommandValidator {
    public void validate(ChangeCourseOfferingCapacityCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.courseOfferingId() == null)
            throw new CommandValidationException("CourseOfferingId cannot be null");

        if (command.newCapacity() <= 0)
            throw new CommandValidationException("Capacity must be greater than zero");
    }
}
