package com.example.universityenrollmentsystem.service.courseOfferings.exam;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RescheduleCourseOfferingExamCommandHandler {

    private final CourseOfferingRepository courseOfferingRepository;
    private final RescheduleCourseOfferingExamCommandValidator validator;

    public RescheduleCourseOfferingExamCommandHandler(CourseOfferingRepository courseOfferingRepository, RescheduleCourseOfferingExamCommandValidator validator) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(RescheduleCourseOfferingExamCommand command) {
        validator.validate(command);
        
        CourseOffering courseOffering = courseOfferingRepository.findById(command.courseOfferingId())
                .orElseThrow(() -> new CourseOfferingNotFoundException(command.courseOfferingId()));
                
        courseOffering.updateExamDate(command.newExamDate());
        courseOfferingRepository.save(courseOffering);
    }
}
