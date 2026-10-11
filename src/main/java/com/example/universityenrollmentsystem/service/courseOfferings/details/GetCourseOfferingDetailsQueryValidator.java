package com.example.universityenrollmentsystem.service.courseOfferings.details;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetCourseOfferingDetailsQueryValidator {
    public void validate(GetCourseOfferingDetailsQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.courseOfferingId() == null)
            throw new CommandValidationException("CourseOfferingId cannot be null");
    }
}
