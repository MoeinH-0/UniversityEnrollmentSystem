package com.example.universityenrollmentsystem.service.courseOfferings.cancel;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelCourseOfferingCommandHandler {

    private final CourseOfferingRepository courseOfferingRepository;
    private final CancelCourseOfferingCommandValidator validator;

    public CancelCourseOfferingCommandHandler(CourseOfferingRepository courseOfferingRepository,
                                              CancelCourseOfferingCommandValidator validator) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(CancelCourseOfferingCommand command) {
        validator.validate(command);
        
        CourseOffering courseOffering = courseOfferingRepository.findById(command.courseOfferingId())
                .orElseThrow(() -> new CourseOfferingNotFoundException(command.courseOfferingId()));
                
        courseOffering.markAsDeleted();
        courseOfferingRepository.save(courseOffering);
    }
}
