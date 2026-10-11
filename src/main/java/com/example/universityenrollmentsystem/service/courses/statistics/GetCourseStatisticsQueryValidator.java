package com.example.universityenrollmentsystem.service.courses.statistics;

import org.springframework.stereotype.Component;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;

@Component
public class GetCourseStatisticsQueryValidator {
    public void validate(GetCourseStatisticsQuery query) {
        if (query == null)
            throw new CommandValidationException("Query cannot be null");

        if (query.courseId() == null)
            throw new CommandValidationException("CourseId cannot be null");
    }
}
