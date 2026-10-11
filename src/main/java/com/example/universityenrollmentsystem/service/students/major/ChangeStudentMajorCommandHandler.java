package com.example.universityenrollmentsystem.service.students.major;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeStudentMajorCommandHandler {

    private final StudentRepository studentRepository;
    private final MajorRepository majorRepository;
    private final ChangeStudentMajorCommandValidator validator;

    public ChangeStudentMajorCommandHandler(StudentRepository studentRepository,
                                            MajorRepository majorRepository,
                                            ChangeStudentMajorCommandValidator validator) {
        this.studentRepository = studentRepository;
        this.majorRepository = majorRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(ChangeStudentMajorCommand command) {
        validator.validate(command);

        Student student = studentRepository.findById(command.studentId())
                .orElseThrow(() -> new StudentNotFoundException(command.studentId()));

        Major newMajor = majorRepository.findById(command.newMajorId())
                .orElseThrow(() -> new MajorNotFoundException(command.newMajorId()));

        student.updateMajor(newMajor);
        studentRepository.save(student);
    }
}
