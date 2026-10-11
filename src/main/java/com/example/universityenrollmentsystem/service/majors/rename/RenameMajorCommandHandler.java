package com.example.universityenrollmentsystem.service.majors.rename;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.MajorAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RenameMajorCommandHandler {

    private final MajorRepository majorRepository;
    private final RenameMajorCommandValidator validator;

    public RenameMajorCommandHandler(MajorRepository majorRepository,
                                     RenameMajorCommandValidator validator) {
        this.majorRepository = majorRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(RenameMajorCommand command) {
        validator.validate(command);

        Major major = majorRepository.findById(command.majorId())
                .orElseThrow(() -> new MajorNotFoundException(command.majorId()));

        if (majorRepository.existsByName(command.newName()))
            throw new MajorAlreadyExistsException(command.newName());

        major.updateName(command.newName());
        majorRepository.save(major);
    }
}
