package com.example.universityenrollmentsystem.service.courseOfferings.guestpolicy;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeCourseOfferingGuestPolicyCommandHandler {

    private final CourseOfferingRepository courseOfferingRepository;
    private final ChangeCourseOfferingGuestPolicyCommandValidator validator;

    public ChangeCourseOfferingGuestPolicyCommandHandler(CourseOfferingRepository courseOfferingRepository,
                                                         ChangeCourseOfferingGuestPolicyCommandValidator validator) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(ChangeCourseOfferingGuestPolicyCommand command) {
        validator.validate(command);
        
        CourseOffering courseOffering = courseOfferingRepository.findById(command.courseOfferingId())
                .orElseThrow(() -> new CourseOfferingNotFoundException(command.courseOfferingId()));
                
        courseOffering.updateGuestPolicy(command.guestAllowed());
        courseOfferingRepository.save(courseOffering);
    }
}
