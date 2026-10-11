package com.example.universityenrollmentsystem.service.semesters.archive;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchiveSemesterCommandHandler {

    private final SemesterRepository semesterRepository;
    private final ArchiveSemesterCommandValidator validator;

    public ArchiveSemesterCommandHandler(SemesterRepository semesterRepository,
                                         ArchiveSemesterCommandValidator validator) {
        this.semesterRepository = semesterRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(ArchiveSemesterCommand command) {
        validator.validate(command);

        Semester semester = semesterRepository.findById(command.semesterId())
                .orElseThrow(() -> new SemesterNotFoundException(command.semesterId()));

        semester.markAsDeleted();
        semesterRepository.save(semester);
    }
}
