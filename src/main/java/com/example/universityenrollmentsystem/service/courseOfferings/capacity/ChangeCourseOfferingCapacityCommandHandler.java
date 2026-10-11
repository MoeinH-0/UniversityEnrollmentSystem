package com.example.universityenrollmentsystem.service.courseOfferings.capacity;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeCourseOfferingCapacityCommandHandler {

    private final CourseOfferingRepository courseOfferingRepository;
    private final EnrollmentRecordRepository enrollmentRecordRepository;
    private final ChangeCourseOfferingCapacityCommandValidator validator;

    public ChangeCourseOfferingCapacityCommandHandler(CourseOfferingRepository courseOfferingRepository,
                                                      EnrollmentRecordRepository enrollmentRecordRepository,
                                                      ChangeCourseOfferingCapacityCommandValidator validator) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.enrollmentRecordRepository = enrollmentRecordRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(ChangeCourseOfferingCapacityCommand command) {
        validator.validate(command);

        CourseOffering courseOffering = courseOfferingRepository.findById(command.courseOfferingId())
                .orElseThrow(() -> new CourseOfferingNotFoundException(command.courseOfferingId()));

        int enrolledCount = enrollmentRecordRepository.findByCourseOfferingId(courseOffering.getId()).size();

        if (command.newCapacity() < enrolledCount)
            throw new CommandValidationException(String.format("Cannot reduce capacity to %d because %d students are already enrolled", command.newCapacity(), enrolledCount));

        courseOffering.updateCapacity(command.newCapacity());
        courseOfferingRepository.save(courseOffering);
    }
}
