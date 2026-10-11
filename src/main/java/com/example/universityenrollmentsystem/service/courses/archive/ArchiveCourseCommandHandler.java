package com.example.universityenrollmentsystem.service.courses.archive;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchiveCourseCommandHandler {

    private final ArchiveCourseCommandValidator validator;
    private final CourseRepository courseRepository;

    public ArchiveCourseCommandHandler(ArchiveCourseCommandValidator validator,
                                       CourseRepository courseRepository) {
        this.validator = validator;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public void handle(ArchiveCourseCommand command) {
        validator.validate(command);

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));

        course.markAsDeleted();
        courseRepository.save(course);
    }
}
