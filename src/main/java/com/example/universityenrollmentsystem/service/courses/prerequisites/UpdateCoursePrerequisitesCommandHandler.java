package com.example.universityenrollmentsystem.service.courses.prerequisites;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UpdateCoursePrerequisitesCommandHandler {

    private final CourseRepository courseRepository;
    private final UpdateCoursePrerequisitesCommandValidator validator;

    public UpdateCoursePrerequisitesCommandHandler(CourseRepository courseRepository,
                                                   UpdateCoursePrerequisitesCommandValidator validator) {
        this.courseRepository = courseRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(UpdateCoursePrerequisitesCommand command) {
        validator.validate(command);

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));

        course.clearPrerequisites();

        if (command.prerequisiteIds() != null && !command.prerequisiteIds().isEmpty()) {
            List<Course> prerequisites = courseRepository.findAllById(command.prerequisiteIds());
            for (Course prereq : prerequisites)
                course.addPrerequisite(prereq);
        }

        courseRepository.save(course);
    }
}
