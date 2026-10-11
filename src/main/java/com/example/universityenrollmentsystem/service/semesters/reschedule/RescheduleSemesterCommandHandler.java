package com.example.universityenrollmentsystem.service.semesters.reschedule;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RescheduleSemesterCommandHandler {

    private final SemesterRepository semesterRepository;
    private final RescheduleSemesterCommandValidator validator;

    public RescheduleSemesterCommandHandler(SemesterRepository semesterRepository,
                                            RescheduleSemesterCommandValidator validator) {
        this.semesterRepository = semesterRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(RescheduleSemesterCommand command) {
        validator.validate(command);

        Semester semester = semesterRepository.findById(command.semesterId())
                .orElseThrow(() -> new SemesterNotFoundException(command.semesterId()));

        semester.reschedule(command.newStartDate(), command.newEndDate());
        semesterRepository.save(semester);
    }
}
