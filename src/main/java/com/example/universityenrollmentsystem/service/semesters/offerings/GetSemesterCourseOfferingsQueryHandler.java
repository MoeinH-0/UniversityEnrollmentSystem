package com.example.universityenrollmentsystem.service.semesters.offerings;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetSemesterCourseOfferingsQueryHandler {

    private final SemesterRepository semesterRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final GetSemesterCourseOfferingsQueryValidator validator;

    public GetSemesterCourseOfferingsQueryHandler(SemesterRepository semesterRepository,
                                                  CourseOfferingRepository courseOfferingRepository,
                                                  GetSemesterCourseOfferingsQueryValidator validator) {
        this.semesterRepository = semesterRepository;
        this.courseOfferingRepository = courseOfferingRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetSemesterCourseOfferingsResponse handle(GetSemesterCourseOfferingsQuery query) {
        validator.validate(query);

        Semester semester = semesterRepository.findById(query.semesterId())
                .orElseThrow(() -> new SemesterNotFoundException(query.semesterId()));

        List<CourseOffering> offerings = courseOfferingRepository.findBySemesterId(semester.getId());

        List<GetSemesterCourseOfferingsResponse.SemesterCourseOfferingDto> dtoList = offerings.stream()
                .filter(offering -> !offering.isDeleted())
                .map(offering -> new GetSemesterCourseOfferingsResponse.SemesterCourseOfferingDto(
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
                ))
                .toList();

        return new GetSemesterCourseOfferingsResponse(semester.getId(), semester.getTitle(), dtoList);
    }
}
