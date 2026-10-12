package com.example.universityenrollmentsystem.service.students.transcript;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStudentTranscriptQueryHandlerTest {

    private static final Long STUDENT_ID = 1L;
    private static final String STUDENT_NAME = "Kasra Alavi";
    private static final String STUDENT_NUMBER = "401123456";
    private static final String MAJOR_NAME = "Computer Engineering";
    private static final Long SEMESTER_ID = 10L;
    private static final String SEMESTER_TITLE = "Fall 2026";
    private static final Long COURSE1_ID = 100L;
    private static final Long COURSE2_ID = 200L;
    private static final int CREDITS_COURSE1 = 3;
    private static final int CREDITS_COURSE2 = 4;
    private static final Double GRADE_PASS = 18.0;
    private static final Double GRADE_FAIL = 8.0;
    private static final double EXPECTED_GPA = 12.29;
    private static final double ZERO_GPA = 0.0;
    private static final int TOTAL_PASSED_CREDITS = 3;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GetStudentTranscriptQueryValidator validator;

    @InjectMocks
    private GetStudentTranscriptQueryHandler handler;

    private Student student;
    private Semester semester;
    private CourseOffering offering1;
    private CourseOffering offering2;

    @BeforeEach
    void setUp() {
        Major major = new Major(MAJOR_NAME);
        student = new Student(STUDENT_NAME, STUDENT_NUMBER, major, EnrollmentPlanType.REGULAR);
        ReflectionTestUtils.setField(student, "id", STUDENT_ID);

        semester = new Semester(SEMESTER_TITLE, LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        ReflectionTestUtils.setField(semester, "id", SEMESTER_ID);

        Course course1 = new Course("Math 1", "MATH101", CREDITS_COURSE1);
        ReflectionTestUtils.setField(course1, "id", COURSE1_ID);
        offering1 = new CourseOffering(course1, semester, "Dr. A", "01", true, 30, "Mon 10:00", null);

        Course course2 = new Course("Physics 1", "PHYS101", CREDITS_COURSE2);
        ReflectionTestUtils.setField(course2, "id", COURSE2_ID);
        offering2 = new CourseOffering(course2, semester, "Dr. B", "01", true, 30, "Tue 10:00", null);
    }

    @SuppressWarnings("unchecked")
    private void addEnrollment(Student s, EnrollmentRecord record) {
        List<EnrollmentRecord> list = (List<EnrollmentRecord>) ReflectionTestUtils.getField(s, "enrollments");
        if (list != null)
            list.add(record);
    }

    @Test
    @DisplayName("Should calculate and return student transcript successfully")
    void shouldReturnStudentTranscriptSuccessfully() {
        GetStudentTranscriptQuery query = new GetStudentTranscriptQuery(STUDENT_ID);

        EnrollmentRecord r1 = new EnrollmentRecord(student, offering1);
        r1.assignGrade(GRADE_PASS);

        EnrollmentRecord r2 = new EnrollmentRecord(student, offering2);
        r2.assignGrade(GRADE_FAIL);

        addEnrollment(student, r1);
        addEnrollment(student, r2);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        GetStudentTranscriptResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.studentId()).isEqualTo(STUDENT_ID);
        assertThat(response.fullName()).isEqualTo(STUDENT_NAME);
        assertThat(response.studentNumber()).isEqualTo(STUDENT_NUMBER);
        assertThat(response.majorName()).isEqualTo(MAJOR_NAME);
        assertThat(response.gpa()).isEqualTo(EXPECTED_GPA);
        assertThat(response.totalPassedCredits()).isEqualTo(TOTAL_PASSED_CREDITS);

        assertThat(response.semesters()).hasSize(1);
        GetStudentTranscriptResponse.SemesterTranscriptDto semDto = response.semesters().get(0);
        assertThat(semDto.semesterId()).isEqualTo(SEMESTER_ID);
        assertThat(semDto.semesterTitle()).isEqualTo(SEMESTER_TITLE);
        assertThat(semDto.semesterGpa()).isEqualTo(EXPECTED_GPA);
        assertThat(semDto.semesterCredits()).isEqualTo(TOTAL_PASSED_CREDITS);
        assertThat(semDto.courses()).hasSize(2);

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should return zero GPA when no grades are recorded")
    void shouldReturnZeroGpaWhenNoGradesRecorded() {
        GetStudentTranscriptQuery query = new GetStudentTranscriptQuery(STUDENT_ID);

        EnrollmentRecord r1 = new EnrollmentRecord(student, offering1);
        addEnrollment(student, r1);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        GetStudentTranscriptResponse response = handler.handle(query);

        assertThat(response.gpa()).isEqualTo(ZERO_GPA);
        assertThat(response.totalPassedCredits()).isEqualTo(0);
        assertThat(response.semesters().get(0).semesterGpa()).isEqualTo(ZERO_GPA);

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when student is not found")
    void shouldThrowExceptionWhenStudentNotFound() {
        GetStudentTranscriptQuery query = new GetStudentTranscriptQuery(STUDENT_ID);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(StudentNotFoundException.class);

        verify(validator).validate(query);
    }
}
