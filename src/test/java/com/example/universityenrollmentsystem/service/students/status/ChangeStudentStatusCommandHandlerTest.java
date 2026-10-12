package com.example.universityenrollmentsystem.service.students.status;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeStudentStatusCommandHandlerTest {

    private static final Long STUDENT_ID = 1L;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ChangeStudentStatusCommandValidator validator;

    @InjectMocks
    private ChangeStudentStatusCommandHandler handler;

    private Student student;

    @BeforeEach
    void setUp() {
        Major major = new Major("Computer Science");
        student = new Student("Ali", "401001", major, EnrollmentPlanType.REGULAR);
    }

    @Test
    @DisplayName("Should successfully change student status")
    void shouldSuccessfullyChangeStatus() {
        ChangeStudentStatusCommand command = new ChangeStudentStatusCommand(STUDENT_ID, StudentStatus.WITHDRAWN);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        handler.handle(command);

        verify(validator).validate(command);
        verify(studentRepository).save(student);
        assertThat(student.getStatus()).isEqualTo(StudentStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when student does not exist")
    void shouldThrowExceptionWhenStudentNotFound() {
        ChangeStudentStatusCommand command = new ChangeStudentStatusCommand(STUDENT_ID, StudentStatus.GRADUATED);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(StudentNotFoundException.class);
    }
}
