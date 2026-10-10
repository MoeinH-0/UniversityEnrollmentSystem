package com.example.universityenrollmentsystem.service.courseOfferings.grade;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.EnrollmentRecordNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmitStudentGradeCommandHandler {

    private final EnrollmentRecordRepository enrollmentRecordRepository;
    private final SubmitStudentGradeCommandValidator validator;

    public SubmitStudentGradeCommandHandler(EnrollmentRecordRepository enrollmentRecordRepository,
                                            SubmitStudentGradeCommandValidator validator) {
        this.enrollmentRecordRepository = enrollmentRecordRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(SubmitStudentGradeCommand command) {
        validator.validate(command);
        
        EnrollmentRecord record = enrollmentRecordRepository.findByStudentIdAndCourseOfferingId(
                command.studentId(), command.courseOfferingId())
                .orElseThrow(() -> new EnrollmentRecordNotFoundException(command.studentId(), command.courseOfferingId()));
                
        record.assignGrade(command.grade());
        enrollmentRecordRepository.save(record);
    }
}
