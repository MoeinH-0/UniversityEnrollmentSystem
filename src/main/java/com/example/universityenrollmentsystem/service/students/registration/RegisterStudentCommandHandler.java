package com.example.universityenrollmentsystem.service.students.registration;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.StudentAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterStudentCommandHandler {

    private final StudentRepository studentRepository;
    private final MajorRepository majorRepository;
    private final RegisterStudentCommandValidator validator;

    public RegisterStudentCommandHandler(StudentRepository studentRepository,
                                         MajorRepository majorRepository,
                                         RegisterStudentCommandValidator validator) {
        this.studentRepository = studentRepository;
        this.majorRepository = majorRepository;
        this.validator = validator;
    }

    @Transactional
    public Long handle(RegisterStudentCommand command) {
        validator.validate(command);

        if (studentRepository.existsByStudentNumber(command.studentNumber()))
            throw new StudentAlreadyExistsException(command.studentNumber());

        Major major = majorRepository.findById(command.majorId())
                .orElseThrow(() -> new MajorNotFoundException(command.majorId()));

        Student student = new Student(command.fullName(), command.studentNumber(), major, command.planType());
        studentRepository.save(student);

        return student.getId();
    }
}
