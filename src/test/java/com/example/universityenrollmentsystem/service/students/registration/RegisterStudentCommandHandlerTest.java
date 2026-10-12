package com.example.universityenrollmentsystem.service.students.registration;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.StudentAlreadyExistsException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterStudentCommandHandlerTest {

    private static final String FULL_NAME = "Ali Rezaei";
    private static final String STUDENT_NUMBER = "401123456";
    private static final Long MAJOR_ID = 1L;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private RegisterStudentCommandValidator validator;

    @InjectMocks
    private RegisterStudentCommandHandler handler;

    private Major major;

    @BeforeEach
    void setUp() {
        major = new Major("Computer Science");
    }

    @Test
    @DisplayName("Should successfully register student when student number is unique and major exists")
    void shouldSuccessfullyRegisterStudent() {
        RegisterStudentCommand command = new RegisterStudentCommand(FULL_NAME, STUDENT_NUMBER, MAJOR_ID, EnrollmentPlanType.REGULAR);

        when(studentRepository.existsByStudentNumber(STUDENT_NUMBER)).thenReturn(false);
        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.of(major));

        handler.handle(command);

        verify(validator).validate(command);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("Should throw StudentAlreadyExistsException when student number already exists")
    void shouldThrowExceptionWhenStudentNumberAlreadyExists() {
        RegisterStudentCommand command = new RegisterStudentCommand(FULL_NAME, STUDENT_NUMBER, MAJOR_ID, EnrollmentPlanType.REGULAR);

        when(studentRepository.existsByStudentNumber(STUDENT_NUMBER)).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(StudentAlreadyExistsException.class);

        verify(validator).validate(command);
        verifyNoInteractions(majorRepository);
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw MajorNotFoundException when major does not exist")
    void shouldThrowExceptionWhenMajorDoesNotExist() {
        RegisterStudentCommand command = new RegisterStudentCommand(FULL_NAME, STUDENT_NUMBER, MAJOR_ID, EnrollmentPlanType.REGULAR);

        when(studentRepository.existsByStudentNumber(STUDENT_NUMBER)).thenReturn(false);
        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(MajorNotFoundException.class);

        verify(studentRepository, never()).save(any());
    }
}
