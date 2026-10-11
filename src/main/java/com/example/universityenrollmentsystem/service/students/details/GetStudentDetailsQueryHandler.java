package com.example.universityenrollmentsystem.service.students.details;

import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetStudentDetailsQueryHandler {

    private final StudentRepository studentRepository;
    private final GetStudentDetailsQueryValidator validator;

    public GetStudentDetailsQueryHandler(StudentRepository studentRepository,
                                         GetStudentDetailsQueryValidator validator) {
        this.studentRepository = studentRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetStudentDetailsResponse handle(GetStudentDetailsQuery query) {
        validator.validate(query);

        Student student = studentRepository.findById(query.studentId())
                .orElseThrow(() -> new StudentNotFoundException(query.studentId()));

        return new GetStudentDetailsResponse(
                student.getId(),
                student.getFullName(),
                student.getStudentNumber(),
                student.getMajor().getId(),
                student.getMajor().getName(),
                student.getPlanType(),
                student.getStatus(),
                student.getEmail(),
                student.getPhoneNumber()
        );
    }
}
