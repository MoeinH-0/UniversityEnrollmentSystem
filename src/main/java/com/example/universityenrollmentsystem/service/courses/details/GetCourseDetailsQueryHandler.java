package com.example.universityenrollmentsystem.service.courses.details;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetCourseDetailsQueryHandler {

    private final CourseRepository courseRepository;
    private final GetCourseDetailsQueryValidator validator;

    public GetCourseDetailsQueryHandler(CourseRepository courseRepository,
                                        GetCourseDetailsQueryValidator validator) {
        this.courseRepository = courseRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetCourseDetailsResponse handle(GetCourseDetailsQuery query) {
        validator.validate(query);

        Course course = courseRepository.findById(query.courseId())
                .orElseThrow(() -> new CourseNotFoundException(query.courseId()));

        List<GetCourseDetailsResponse.PrerequisiteDto> prerequisites = course.getPrerequisites()
                .stream()
                .map(prereq -> new GetCourseDetailsResponse.PrerequisiteDto(
                        prereq.getId(),
                        prereq.getName(),
                        prereq.getCourseCode(),
                        prereq.getCredits()
                ))
                .toList();

        List<String> majors = course.getMajors().stream()
                .map(Major::getName)
                .toList();

        return new GetCourseDetailsResponse(
                course.getId(),
                course.getName(),
                course.getCourseCode(),
                course.getCredits(),
                course.isDeleted(),
                prerequisites,
                majors
        );
    }
}
