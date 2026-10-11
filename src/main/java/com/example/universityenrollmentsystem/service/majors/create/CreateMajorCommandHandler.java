package com.example.universityenrollmentsystem.service.majors.create;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.MajorAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateMajorCommandHandler {

    private final MajorRepository majorRepository;
    private final CreateMajorCommandValidator validator;

    public CreateMajorCommandHandler(MajorRepository majorRepository,
                                     CreateMajorCommandValidator validator) {
        this.majorRepository = majorRepository;
        this.validator = validator;
    }

    @Transactional
    public Long handle(CreateMajorCommand command) {
        validator.validate(command);

        if (majorRepository.existsByName(command.name()))
            throw new MajorAlreadyExistsException(command.name());

        Major major = new Major(command.name());
        majorRepository.save(major);

        return major.getId();
    }
}
