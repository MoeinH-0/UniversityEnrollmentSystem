package com.example.universityenrollmentsystem.service.majors.addcourse;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddCourseToMajorCommandHandler {

    private final MajorRepository majorRepository;
    private final CourseRepository courseRepository;
    private final AddCourseToMajorCommandValidator validator;

    public AddCourseToMajorCommandHandler(MajorRepository majorRepository,
                                          CourseRepository courseRepository,
                                          AddCourseToMajorCommandValidator validator) {
        this.majorRepository = majorRepository;
        this.courseRepository = courseRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(AddCourseToMajorCommand command) {
        validator.validate(command);

        Major major = majorRepository.findById(command.majorId())
                .orElseThrow(() -> new MajorNotFoundException(command.majorId()));

        if (major.isDeleted())
            throw new CommandValidationException("Cannot add course to an archived major");

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));

        if (course.isDeleted())
            throw new CommandValidationException("Cannot add an archived course to a major");

        major.addCourse(course);
        majorRepository.save(major);
    }
}
