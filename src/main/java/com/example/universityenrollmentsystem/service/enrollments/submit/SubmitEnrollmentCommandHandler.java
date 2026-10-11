package com.example.universityenrollmentsystem.service.enrollments.submit;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import com.example.universityenrollmentsystem.domain.service.enrollment.EnrollmentPlanFactory;
import com.example.universityenrollmentsystem.domain.service.enrollment.EnrollmentPlanStrategy;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.CourseOfferingFullException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubmitEnrollmentCommandHandler {

    private final CourseOfferingRepository courseOfferingRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRecordRepository enrollmentRecordRepository;
    private final EnrollmentPlanFactory enrollmentPlanFactory;
    private final SubmitEnrollmentCommandValidator validator;

    public SubmitEnrollmentCommandHandler(CourseOfferingRepository courseOfferingRepository,
                                          StudentRepository studentRepository,
                                          EnrollmentRecordRepository enrollmentRecordRepository,
                                          EnrollmentPlanFactory enrollmentPlanFactory,
                                          SubmitEnrollmentCommandValidator validator) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRecordRepository = enrollmentRecordRepository;
        this.enrollmentPlanFactory = enrollmentPlanFactory;
        this.validator = validator;
    }

    @Transactional
    public void handle(SubmitEnrollmentCommand command) {
        validator.validate(command);

        Student student = studentRepository.findById(command.studentId())
                .orElseThrow(() -> new StudentNotFoundException(command.studentId()));

        if (student.getStatus() != StudentStatus.ACTIVE)
            throw new CommandValidationException("Only active students can enroll in courses");

        CourseOffering courseOffering = courseOfferingRepository.findById(command.courseOfferingId())
                .orElseThrow(() -> new CourseOfferingNotFoundException(command.courseOfferingId()));

        if (courseOffering.isDeleted())
            throw new CommandValidationException("Cannot enroll in a cancelled or deleted course offering");

        int currentEnrolledCount = enrollmentRecordRepository.findByCourseOfferingId(courseOffering.getId()).size();

        if (currentEnrolledCount >= courseOffering.getCapacity())
            throw new CourseOfferingFullException(courseOffering.getId());

        EnrollmentPlanStrategy strategy = enrollmentPlanFactory.getStrategy(student.getPlanType());

        student.enroll(List.of(courseOffering), strategy);

        studentRepository.save(student);
    }
}
