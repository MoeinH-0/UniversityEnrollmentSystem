package com.example.universityenrollmentsystem.service.majors.archive;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchiveMajorCommandHandler {

    private final MajorRepository majorRepository;
    private final ArchiveMajorCommandValidator validator;

    public ArchiveMajorCommandHandler(MajorRepository majorRepository,
                                      ArchiveMajorCommandValidator validator) {
        this.majorRepository = majorRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(ArchiveMajorCommand command) {
        validator.validate(command);

        Major major = majorRepository.findById(command.majorId())
                .orElseThrow(() -> new MajorNotFoundException(command.majorId()));

        major.markAsDeleted();
        majorRepository.save(major);
    }
}
