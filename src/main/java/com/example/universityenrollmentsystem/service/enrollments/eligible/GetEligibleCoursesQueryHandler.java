package com.example.universityenrollmentsystem.service.enrollments.eligible;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetEligibleCoursesQueryHandler {

    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final GetEligibleCoursesQueryValidator validator;
    private final CourseEligibilityChecker eligibilityChecker;

    public GetEligibleCoursesQueryHandler(StudentRepository studentRepository,
                                          SemesterRepository semesterRepository,
                                          CourseOfferingRepository courseOfferingRepository,
                                          GetEligibleCoursesQueryValidator validator,
                                          CourseEligibilityChecker eligibilityChecker) {
        this.studentRepository = studentRepository;
        this.semesterRepository = semesterRepository;
        this.courseOfferingRepository = courseOfferingRepository;
        this.validator = validator;
        this.eligibilityChecker = eligibilityChecker;
    }

    @Transactional(readOnly = true)
    public GetEligibleCoursesResponse handle(GetEligibleCoursesQuery query) {
        validator.validate(query);

        Student student = studentRepository.findById(query.studentId())
                .orElseThrow(() -> new StudentNotFoundException(query.studentId()));

        Semester semester = semesterRepository.findById(query.semesterId())
                .orElseThrow(() -> new SemesterNotFoundException(query.semesterId()));

        List<CourseOffering> offerings = courseOfferingRepository.findBySemesterId(semester.getId());

        List<GetEligibleCoursesResponse.EligibleCourseOfferingDto> eligibleOfferings = offerings.stream()
                .filter(offering -> !offering.isDeleted())
                .filter(offering -> eligibilityChecker.isEligible(student, offering))
                .map(this::toDto)
                .toList();

        return new GetEligibleCoursesResponse(student.getId(), semester.getId(), eligibleOfferings);
    }

    private GetEligibleCoursesResponse.EligibleCourseOfferingDto toDto(CourseOffering offering) {
        return new GetEligibleCoursesResponse.EligibleCourseOfferingDto(
                offering.getId(),
                offering.getCourse().getId(),
                offering.getCourse().getName(),
                offering.getCourse().getCourseCode(),
                offering.getCourse().getCredits(),
                offering.getProfessorName(),
                offering.getSection(),
                offering.getCapacity(),
                offering.getClassTime(),
                offering.getExamDate(),
                offering.isGuestAllowed()
        );
    }
}
