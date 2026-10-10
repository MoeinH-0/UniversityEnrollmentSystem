package com.example.universityenrollmentsystem.service.courseOfferings.students;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetCourseOfferingStudentsQueryHandler {

    private final EnrollmentRecordRepository enrollmentRecordRepository;
    private final GetCourseOfferingStudentsQueryValidator validator;

    public GetCourseOfferingStudentsQueryHandler(EnrollmentRecordRepository enrollmentRecordRepository,
                                                 GetCourseOfferingStudentsQueryValidator validator) {
        this.enrollmentRecordRepository = enrollmentRecordRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetCourseOfferingStudentsResponse handle(GetCourseOfferingStudentsQuery query) {
        validator.validate(query);
        
        List<EnrollmentRecord> records = enrollmentRecordRepository.
                findByCourseOfferingId(query.courseOfferingId());
        
        List<GetCourseOfferingStudentsResponse.CourseOfferingStudentDto> dtoList = records.stream()
                .map(record -> new GetCourseOfferingStudentsResponse.CourseOfferingStudentDto(
                        record.getStudent().getId(),
                        record.getStudent().getFullName(),
                        record.getStudent().getStudentNumber(),
                        record.getGrade(),
                        record.getStatus()
                ))
                .toList();
                
        return new GetCourseOfferingStudentsResponse(query.courseOfferingId(), dtoList);
    }
}
