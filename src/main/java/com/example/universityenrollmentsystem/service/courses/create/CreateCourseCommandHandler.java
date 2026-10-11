package com.example.universityenrollmentsystem.service.courses.create;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.CourseAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreateCourseCommandHandler {

    private final CourseRepository courseRepository;
    private final MajorRepository majorRepository;
    private final CreateCourseCommandValidator validator;

    public CreateCourseCommandHandler(CourseRepository courseRepository,
                                      MajorRepository majorRepository,
                                      CreateCourseCommandValidator validator) {
        this.courseRepository = courseRepository;
        this.majorRepository = majorRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(CreateCourseCommand command) {
        validator.validate(command);

        if (courseRepository.existsByCourseCode(command.courseCode()))
            throw new CourseAlreadyExistsException(command.courseCode());

        List<Major> majors = majorRepository.findAllById(command.majorIds());

        if (majors.size() != command.majorIds().size())
            throw new CommandValidationException("One or more majors not found");

        Course course = new Course(command.name(), command.courseCode(), command.credits());

        if (command.prerequisiteIds() != null && !command.prerequisiteIds().isEmpty()) {
            List<Course> prerequisites = courseRepository.findAllById(command.prerequisiteIds());
            for (Course prereq : prerequisites)
                course.addPrerequisite(prereq);
        }

        courseRepository.save(course);

        for (Major major : majors) {
            major.addCourse(course);
            majorRepository.save(major);
        }
    }
}
