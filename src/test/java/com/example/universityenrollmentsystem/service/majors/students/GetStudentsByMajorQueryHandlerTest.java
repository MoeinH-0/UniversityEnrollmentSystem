package com.example.universityenrollmentsystem.service.majors.students;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStudentsByMajorQueryHandlerTest {

    private static final Long MAJOR_ID = 1L;
    private static final String MAJOR_NAME = "Computer Science";
    private static final Long STUDENT_ID = 100L;
    private static final String STUDENT_NAME = "Sara Ahmadi";
    private static final String STUDENT_NUMBER = "401234";

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GetStudentsByMajorQueryValidator validator;

    @InjectMocks
    private GetStudentsByMajorQueryHandler handler;

    private Major major;
    private Student student;

    @BeforeEach
    void setUp() {
        major = new Major(MAJOR_NAME);
        ReflectionTestUtils.setField(major, "id", MAJOR_ID);

        student = new Student(STUDENT_NAME, STUDENT_NUMBER, major, EnrollmentPlanType.REGULAR);
        ReflectionTestUtils.setField(student, "id", STUDENT_ID);
    }

    @Test
    @DisplayName("Should return students by major successfully")
    void shouldReturnStudentsByMajorSuccessfully() {
        GetStudentsByMajorQuery query = new GetStudentsByMajorQuery(MAJOR_ID);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.of(major));
        when(studentRepository.findByMajorId(MAJOR_ID)).thenReturn(List.of(student));

        GetStudentsByMajorResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.majorId()).isEqualTo(MAJOR_ID);
        assertThat(response.majorName()).isEqualTo(MAJOR_NAME);
        assertThat(response.students()).hasSize(1);

        GetStudentsByMajorResponse.MajorStudentSummaryDto dto = response.students().get(0);
        assertThat(dto.studentId()).isEqualTo(STUDENT_ID);
        assertThat(dto.fullName()).isEqualTo(STUDENT_NAME);
        assertThat(dto.studentNumber()).isEqualTo(STUDENT_NUMBER);
        assertThat(dto.planType()).isEqualTo(EnrollmentPlanType.REGULAR);
        assertThat(dto.status()).isEqualTo(StudentStatus.ACTIVE);

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should throw MajorNotFoundException when major is not found")
    void shouldThrowExceptionWhenMajorNotFound() {
        GetStudentsByMajorQuery query = new GetStudentsByMajorQuery(MAJOR_ID);
        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(MajorNotFoundException.class);

        verify(validator).validate(query);
    }
}
