package com.example.universityenrollmentsystem.service.semesters.details;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSemesterDetailsQueryHandler {

    private final SemesterRepository semesterRepository;
    private final GetSemesterDetailsQueryValidator validator;

    public GetSemesterDetailsQueryHandler(SemesterRepository semesterRepository,
                                          GetSemesterDetailsQueryValidator validator) {
        this.semesterRepository = semesterRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetSemesterDetailsResponse handle(GetSemesterDetailsQuery query) {
        validator.validate(query);

        Semester semester = semesterRepository.findById(query.semesterId())
                .orElseThrow(() -> new SemesterNotFoundException(query.semesterId()));

        return new GetSemesterDetailsResponse(
                semester.getId(),
                semester.getTitle(),
                semester.getStartDate(),
                semester.getEndDate(),
                semester.isDeleted()
        );
    }
}
