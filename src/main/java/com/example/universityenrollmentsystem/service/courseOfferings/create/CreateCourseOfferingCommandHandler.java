package com.example.universityenrollmentsystem.service.courseOfferings.create;

import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCourseOfferingCommandHandler {

    private final CourseOfferingRepository courseOfferingRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final CreateCourseOfferingCommandValidator validator;

    public CreateCourseOfferingCommandHandler(CourseOfferingRepository courseOfferingRepository, 
                                              CourseRepository courseRepository, 
                                              SemesterRepository semesterRepository, 
                                              CreateCourseOfferingCommandValidator validator) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.courseRepository = courseRepository;
        this.semesterRepository = semesterRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(CreateCourseOfferingCommand command) {
        validator.validate(command);
        
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new CourseNotFoundException(command.courseId()));
                
        Semester semester = semesterRepository.findById(command.semesterId())
                .orElseThrow(() -> new SemesterNotFoundException(command.semesterId()));
                
        CourseOffering courseOffering = new CourseOffering(
                course,
                semester,
                command.professorName(),
                command.section(),
                command.guestAllowed(),
                command.capacity(),
                command.classTime(),
                command.examDate()
        );
        
        courseOfferingRepository.save(courseOffering);
    }
}
