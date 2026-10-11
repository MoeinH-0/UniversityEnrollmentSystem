package com.example.universityenrollmentsystem.service.enrollments.drop;

import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DropCourseOfferingCommandHandler {

    private final CourseOfferingRepository courseOfferingRepository;
    private final StudentRepository studentRepository;
    private final DropCourseOfferingCommandValidator validator;

    public DropCourseOfferingCommandHandler(CourseOfferingRepository courseOfferingRepository,
                                            StudentRepository studentRepository,
                                            DropCourseOfferingCommandValidator validator) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.studentRepository = studentRepository;
        this.validator = validator;
    }

    @Transactional
    public void handle(DropCourseOfferingCommand command) {
        validator.validate(command);

        Student student = studentRepository.findById(command.studentId())
                .orElseThrow(() -> new StudentNotFoundException(command.studentId()));

        CourseOffering courseOffering = courseOfferingRepository.findById(command.courseOfferingId())
                .orElseThrow(() -> new CourseOfferingNotFoundException(command.courseOfferingId()));

        student.drop(courseOffering);
        studentRepository.save(student);
    }
}
