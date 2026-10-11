package com.example.universityenrollmentsystem.service.courses.details;

import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Component;

@Component
public class GetCourseDetailsQueryValidator {
    public void validate(GetCourseDetailsQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.courseId() == null)
            throw new CommandValidationException("CourseId cannot be null");
    }
}
