package com.example.universityenrollmentsystem.service.courseOfferings.capacity;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeCourseOfferingCapacityCommandHandler {

    private final CourseOfferingRepository courseOfferingRepository;
    private final ChangeCourseOfferingCapacityCommandValidator validator;

    public ChangeCourseOfferingCapacityCommandHandler(CourseOfferingRepository courseOfferingRepository, ChangeCourseOfferingCapacityCommandValidator validator) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(ChangeCourseOfferingCapacityCommand command) {
        validator.validate(command);
        
        CourseOffering courseOffering = courseOfferingRepository.findById(command.courseOfferingId())
                .orElseThrow(() -> new CourseOfferingNotFoundException(command.courseOfferingId()));
                
        courseOffering.updateCapacity(command.newCapacity());
        courseOfferingRepository.save(courseOffering);
    }
}
