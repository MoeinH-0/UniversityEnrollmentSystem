package com.example.universityenrollmentsystem.service.courseOfferings.students;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class GetCourseOfferingStudentsQueryValidator {
    public void validate(GetCourseOfferingStudentsQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.courseOfferingId() == null)
            throw new CommandValidationException("CourseOfferingId cannot be null");
    }
}
