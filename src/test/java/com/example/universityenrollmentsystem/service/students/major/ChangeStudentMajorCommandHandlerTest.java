package com.example.universityenrollmentsystem.service.students.major;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeStudentMajorCommandHandlerTest {

    private static final Long STUDENT_ID = 1L;
    private static final Long NEW_MAJOR_ID = 2L;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private ChangeStudentMajorCommandValidator validator;

    @InjectMocks
    private ChangeStudentMajorCommandHandler handler;

    private Student student;
    private Major currentMajor;
    private Major newMajor;

    @BeforeEach
    void setUp() {
        currentMajor = new Major("Computer Science");
        newMajor = new Major("Electrical Engineering");
        student = new Student("Ali", "401001", currentMajor, EnrollmentPlanType.REGULAR);
    }

    @Test
    @DisplayName("Should successfully change student major")
    void shouldSuccessfullyChangeMajor() {
        ChangeStudentMajorCommand command = new ChangeStudentMajorCommand(STUDENT_ID, NEW_MAJOR_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(majorRepository.findById(NEW_MAJOR_ID)).thenReturn(Optional.of(newMajor));

        handler.handle(command);

        verify(validator).validate(command);
        verify(studentRepository).save(student);
        assertThat(student.getMajor()).isEqualTo(newMajor);
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when student does not exist")
    void shouldThrowExceptionWhenStudentNotFound() {
        ChangeStudentMajorCommand command = new ChangeStudentMajorCommand(STUDENT_ID, NEW_MAJOR_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(StudentNotFoundException.class);

        verifyNoInteractions(majorRepository);
    }

    @Test
    @DisplayName("Should throw MajorNotFoundException when new major does not exist")
    void shouldThrowExceptionWhenNewMajorNotFound() {
        ChangeStudentMajorCommand command = new ChangeStudentMajorCommand(STUDENT_ID, NEW_MAJOR_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(majorRepository.findById(NEW_MAJOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(MajorNotFoundException.class);

        verify(studentRepository, never()).save(any());
    }
}
