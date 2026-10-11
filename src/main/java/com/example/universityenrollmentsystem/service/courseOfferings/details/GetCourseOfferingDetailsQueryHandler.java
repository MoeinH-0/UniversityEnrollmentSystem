package com.example.universityenrollmentsystem.service.courseOfferings.details;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCourseOfferingDetailsQueryHandler {

    private final CourseOfferingRepository courseOfferingRepository;
    private final EnrollmentRecordRepository enrollmentRecordRepository;
    private final GetCourseOfferingDetailsQueryValidator validator;

    public GetCourseOfferingDetailsQueryHandler(CourseOfferingRepository courseOfferingRepository,
                                                EnrollmentRecordRepository enrollmentRecordRepository,
                                                GetCourseOfferingDetailsQueryValidator validator) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.enrollmentRecordRepository = enrollmentRecordRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetCourseOfferingDetailsResponse handle(GetCourseOfferingDetailsQuery query) {
        validator.validate(query);

        CourseOffering offering = courseOfferingRepository.findById(query.courseOfferingId())
                .orElseThrow(() -> new CourseOfferingNotFoundException(query.courseOfferingId()));

        int enrolledCount = enrollmentRecordRepository.findByCourseOfferingId(offering.getId()).size();

        return new GetCourseOfferingDetailsResponse(
                offering.getId(),
                offering.getCourse().getId(),
                offering.getCourse().getName(),
                offering.getCourse().getCourseCode(),
                offering.getCourse().getCredits(),
                offering.getSemester().getId(),
                offering.getSemester().getTitle(),
                offering.getProfessorName(),
                offering.getSection(),
                offering.isGuestAllowed(),
                offering.getCapacity(),
                enrolledCount,
                offering.getClassTime(),
                offering.getExamDate(),
                offering.isDeleted()
        );
    }
}
