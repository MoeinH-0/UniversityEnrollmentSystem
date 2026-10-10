package com.example.universityenrollmentsystem.service.courseOfferings.professor;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeCourseOfferingProfessorCommandHandler {

    private final CourseOfferingRepository courseOfferingRepository;
    private final ChangeCourseOfferingProfessorCommandValidator validator;

    public ChangeCourseOfferingProfessorCommandHandler(CourseOfferingRepository courseOfferingRepository, ChangeCourseOfferingProfessorCommandValidator validator) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(ChangeCourseOfferingProfessorCommand command) {
        validator.validate(command);
        
        CourseOffering courseOffering = courseOfferingRepository.findById(command.courseOfferingId())
                .orElseThrow(() -> new CourseOfferingNotFoundException(command.courseOfferingId()));
                
        courseOffering.changeProfessor(command.newProfessorName());
        courseOfferingRepository.save(courseOffering);
    }
}
