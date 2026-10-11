package com.example.universityenrollmentsystem.service.semesters.create;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.SemesterAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSemesterCommandHandler {

    private final SemesterRepository semesterRepository;
    private final CreateSemesterCommandValidator validator;

    public CreateSemesterCommandHandler(SemesterRepository semesterRepository,
                                        CreateSemesterCommandValidator validator) {
        this.semesterRepository = semesterRepository;
        this.validator = validator;
    }

    @Transactional
    public Long handle(CreateSemesterCommand command) {
        validator.validate(command);

        if (semesterRepository.existsByTitle(command.title()))
            throw new SemesterAlreadyExistsException(command.title());

        Semester semester = new Semester(command.title(), command.startDate(), command.endDate());
        semesterRepository.save(semester);

        return semester.getId();
    }
}
