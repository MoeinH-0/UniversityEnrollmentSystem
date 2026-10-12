package com.example.universityenrollmentsystem.service.students.details;

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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStudentDetailsQueryHandlerTest {

    private static final Long STUDENT_ID = 1L;
    private static final String FULL_NAME = "Kasra Alavi";
    private static final String STUDENT_NUMBER = "401123456";
    private static final Long MAJOR_ID = 10L;
    private static final String MAJOR_NAME = "Computer Engineering";
    private static final String EMAIL = "kasra@university.edu";
    private static final String PHONE = "09121234567";

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GetStudentDetailsQueryValidator validator;

    @InjectMocks
    private GetStudentDetailsQueryHandler handler;

    private Student student;

    @BeforeEach
    void setUp() {
        Major major = new Major(MAJOR_NAME);
        ReflectionTestUtils.setField(major, "id", MAJOR_ID);

        student = new Student(FULL_NAME, STUDENT_NUMBER, major, EnrollmentPlanType.REGULAR);
        student.updateContactInfo(EMAIL, PHONE);
        ReflectionTestUtils.setField(student, "id", STUDENT_ID);
    }

    @Test
    @DisplayName("Should return student details successfully")
    void shouldReturnStudentDetailsSuccessfully() {
        GetStudentDetailsQuery query = new GetStudentDetailsQuery(STUDENT_ID);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        GetStudentDetailsResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.studentId()).isEqualTo(STUDENT_ID);
        assertThat(response.fullName()).isEqualTo(FULL_NAME);
        assertThat(response.studentNumber()).isEqualTo(STUDENT_NUMBER);
        assertThat(response.majorId()).isEqualTo(MAJOR_ID);
        assertThat(response.majorName()).isEqualTo(MAJOR_NAME);
        assertThat(response.planType()).isEqualTo(EnrollmentPlanType.REGULAR);
        assertThat(response.status()).isEqualTo(StudentStatus.ACTIVE);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.phoneNumber()).isEqualTo(PHONE);

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when student is not found")
    void shouldThrowExceptionWhenStudentNotFound() {
        GetStudentDetailsQuery query = new GetStudentDetailsQuery(STUDENT_ID);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(StudentNotFoundException.class);

        verify(validator).validate(query);
    }
}
