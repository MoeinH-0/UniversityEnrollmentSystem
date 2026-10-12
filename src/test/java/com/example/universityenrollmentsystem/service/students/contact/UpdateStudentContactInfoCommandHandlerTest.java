package com.example.universityenrollmentsystem.service.students.contact;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStudentContactInfoCommandHandlerTest {

    private static final Long STUDENT_ID = 1L;
    private static final String EMAIL = "student@university.ac.ir";
    private static final String PHONE = "09123456789";

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UpdateStudentContactInfoCommandValidator validator;

    @InjectMocks
    private UpdateStudentContactInfoCommandHandler handler;

    private Student student;

    @BeforeEach
    void setUp() {
        Major major = new Major("Computer Engineering");
        student = new Student("Ali Rezaei", "401123456", major, EnrollmentPlanType.REGULAR);
    }

    @Test
    @DisplayName("Should update contact info successfully")
    void shouldUpdateContactInfoSuccessfully() {
        UpdateStudentContactInfoCommand command = new UpdateStudentContactInfoCommand(STUDENT_ID, EMAIL, PHONE);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        handler.handle(command);

        assertThat(student.getEmail()).isEqualTo(EMAIL);
        assertThat(student.getPhoneNumber()).isEqualTo(PHONE);
        verify(validator).validate(command);
        verify(studentRepository).save(student);
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when student is not found")
    void shouldThrowExceptionWhenStudentNotFound() {
        UpdateStudentContactInfoCommand command = new UpdateStudentContactInfoCommand(STUDENT_ID, EMAIL, PHONE);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(StudentNotFoundException.class);

        verify(validator).validate(command);
        verify(studentRepository, never()).save(any());
    }
}
