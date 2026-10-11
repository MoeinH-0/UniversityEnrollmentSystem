package com.example.universityenrollmentsystem.service.majors.students;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetStudentsByMajorQueryHandler {

    private final MajorRepository majorRepository;
    private final StudentRepository studentRepository;
    private final GetStudentsByMajorQueryValidator validator;

    public GetStudentsByMajorQueryHandler(MajorRepository majorRepository,
                                          StudentRepository studentRepository,
                                          GetStudentsByMajorQueryValidator validator) {
        this.majorRepository = majorRepository;
        this.studentRepository = studentRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetStudentsByMajorResponse handle(GetStudentsByMajorQuery query) {
        validator.validate(query);

        Major major = majorRepository.findById(query.majorId())
                .orElseThrow(() -> new MajorNotFoundException(query.majorId()));

        List<Student> students = studentRepository.findByMajorId(major.getId());

        List<GetStudentsByMajorResponse.MajorStudentSummaryDto> dtoList = students.stream()
                .map(student -> new GetStudentsByMajorResponse.MajorStudentSummaryDto(
                        student.getId(),
                        student.getFullName(),
                        student.getStudentNumber(),
                        student.getPlanType(),
                        student.getStatus()
                ))
                .toList();

        return new GetStudentsByMajorResponse(major.getId(), major.getName(), dtoList);
    }
}
