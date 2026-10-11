package com.example.universityenrollmentsystem.service.courses.credits;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCourseCreditsCommandHandler {

    private final CourseRepository courseRepository;
    private final UpdateCourseCreditsCommandValidator validator;

    public UpdateCourseCreditsCommandHandler(CourseRepository courseRepository,
                                             UpdateCourseCreditsCommandValidator validator) {
        this.courseRepository = courseRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(UpdateCourseCreditsCommand command) {
        validator.validate(command);

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));

        course.updateCredits(command.newCredits());
        courseRepository.save(course);
    }
}
