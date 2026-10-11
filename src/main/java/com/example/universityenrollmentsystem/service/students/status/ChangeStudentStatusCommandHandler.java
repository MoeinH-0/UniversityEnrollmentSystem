package com.example.universityenrollmentsystem.service.students.status;

import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeStudentStatusCommandHandler {

    private final StudentRepository studentRepository;
    private final ChangeStudentStatusCommandValidator validator;

    public ChangeStudentStatusCommandHandler(StudentRepository studentRepository,
                                             ChangeStudentStatusCommandValidator validator) {
        this.studentRepository = studentRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(ChangeStudentStatusCommand command) {
        validator.validate(command);

        Student student = studentRepository.findById(command.studentId())
                .orElseThrow(() -> new StudentNotFoundException(command.studentId()));

        student.updateStatus(command.newStatus());
        studentRepository.save(student);
    }
}
