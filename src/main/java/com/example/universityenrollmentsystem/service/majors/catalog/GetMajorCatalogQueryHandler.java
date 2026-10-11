package com.example.universityenrollmentsystem.service.majors.catalog;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetMajorCatalogQueryHandler {

    private final MajorRepository majorRepository;
    private final GetMajorCatalogQueryValidator validator;

    public GetMajorCatalogQueryHandler(MajorRepository majorRepository,
                                       GetMajorCatalogQueryValidator validator) {
        this.majorRepository = majorRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetMajorCatalogResponse handle(GetMajorCatalogQuery query) {
        validator.validate(query);

        Major major = majorRepository.findById(query.majorId())
                .orElseThrow(() -> new MajorNotFoundException(query.majorId()));

        List<GetMajorCatalogResponse.MajorCatalogCourseDto> dtoList = major.getCourses().stream()
                .filter(course -> !course.isDeleted())
                .map(course -> new GetMajorCatalogResponse.MajorCatalogCourseDto(
                        course.getId(),
                        course.getName(),
                        course.getCourseCode(),
                        course.getCredits()
                ))
                .toList();

        return new GetMajorCatalogResponse(major.getId(), major.getName(), dtoList);
    }
}
