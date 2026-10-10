package com.example.universityenrollmentsystem.service.courseOfferings.guestpolicy;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class ChangeCourseOfferingGuestPolicyCommandValidator {
    public void validate(ChangeCourseOfferingGuestPolicyCommand command) {
        if (command == null)
            throw new CommandValidationException("Command cannot be null");

        if (command.courseOfferingId() == null)
            throw new CommandValidationException("CourseOfferingId cannot be null");
    }
}
