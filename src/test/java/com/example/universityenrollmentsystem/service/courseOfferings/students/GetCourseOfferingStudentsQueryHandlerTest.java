package com.example.universityenrollmentsystem.service.courseOfferings.students;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCourseOfferingStudentsQueryHandlerTest {

    private static final Long OFFERING_ID = 100L;
    private static final Long STUDENT_ID = 5L;
    private static final String STUDENT_NAME = "Ali Rezaei";
    private static final String STUDENT_NUMBER = "401111";
    private static final Double GRADE = 17.5;

    @Mock
    private EnrollmentRecordRepository enrollmentRecordRepository;

    @Mock
    private GetCourseOfferingStudentsQueryValidator validator;

    @InjectMocks
    private GetCourseOfferingStudentsQueryHandler handler;

    private CourseOffering offering;
    private Student student;

    @BeforeEach
    void setUp() {
        Course course = new Course("Software Engineering", "SE301", 3);
        Semester semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        offering = new CourseOffering(course, semester, "Dr. Moradi", "01", true, 30, "Sun 14:00", null);
        ReflectionTestUtils.setField(offering, "id", OFFERING_ID);

        Major major = new Major("Computer Engineering");
        student = new Student(STUDENT_NAME, STUDENT_NUMBER, major, EnrollmentPlanType.REGULAR);
        ReflectionTestUtils.setField(student, "id", STUDENT_ID);
    }

    @Test
    @DisplayName("Should return enrolled students successfully")
    void shouldReturnEnrolledStudentsSuccessfully() {
        GetCourseOfferingStudentsQuery query = new GetCourseOfferingStudentsQuery(OFFERING_ID);

        EnrollmentRecord record = new EnrollmentRecord(student, offering);
        record.assignGrade(GRADE);

        when(enrollmentRecordRepository.findByCourseOfferingId(OFFERING_ID)).thenReturn(List.of(record));

        GetCourseOfferingStudentsResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.courseOfferingId()).isEqualTo(OFFERING_ID);
        assertThat(response.students()).hasSize(1);

        GetCourseOfferingStudentsResponse.CourseOfferingStudentDto dto = response.students().get(0);
        assertThat(dto.studentId()).isEqualTo(STUDENT_ID);
        assertThat(dto.fullName()).isEqualTo(STUDENT_NAME);
        assertThat(dto.studentNumber()).isEqualTo(STUDENT_NUMBER);
        assertThat(dto.grade()).isEqualTo(GRADE);
        assertThat(dto.courseStatus()).isEqualTo(CourseStatus.PASSED);

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should return empty list when no students are enrolled")
    void shouldReturnEmptyListWhenNoStudentsEnrolled() {
        GetCourseOfferingStudentsQuery query = new GetCourseOfferingStudentsQuery(OFFERING_ID);

        when(enrollmentRecordRepository.findByCourseOfferingId(OFFERING_ID)).thenReturn(Collections.emptyList());

        GetCourseOfferingStudentsResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.courseOfferingId()).isEqualTo(OFFERING_ID);
        assertThat(response.students()).isEmpty();

        verify(validator).validate(query);
    }
}
