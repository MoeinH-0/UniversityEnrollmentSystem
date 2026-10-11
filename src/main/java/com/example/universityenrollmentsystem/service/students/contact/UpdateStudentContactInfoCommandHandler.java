package com.example.universityenrollmentsystem.service.students.contact;

import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateStudentContactInfoCommandHandler {

    private final StudentRepository studentRepository;
    private final UpdateStudentContactInfoCommandValidator validator;

    public UpdateStudentContactInfoCommandHandler(StudentRepository studentRepository,
                                                  UpdateStudentContactInfoCommandValidator validator) {
        this.studentRepository = studentRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(UpdateStudentContactInfoCommand command) {
        validator.validate(command);

        Student student = studentRepository.findById(command.studentId())
                .orElseThrow(() -> new StudentNotFoundException(command.studentId()));

        student.updateContactInfo(command.email(), command.phoneNumber());
        studentRepository.save(student);
    }
}
