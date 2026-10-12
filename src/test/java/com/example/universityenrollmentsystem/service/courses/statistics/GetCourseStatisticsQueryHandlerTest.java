package com.example.universityenrollmentsystem.service.courses.statistics;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCourseStatisticsQueryHandlerTest {

    private static final Long COURSE_ID = 1L;
    private static final Long MAJOR_CE_ID = 10L;
    private static final Long MAJOR_EE_ID = 20L;
    private static final String COURSE_NAME = "Database Systems";
    private static final String COURSE_CODE = "CS305";
    private static final int CREDITS = 3;
    private static final int TOTAL_OFFERINGS = 2;
    private static final Double GRADE_PASS = 18.0;
    private static final Double GRADE_FAIL = 8.0;
    private static final double EXPECTED_AVERAGE = 13.0;
    private static final double ZERO_AVERAGE = 0.0;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private EnrollmentRecordRepository enrollmentRecordRepository;

    @Mock
    private GetCourseStatisticsQueryValidator validator;

    @InjectMocks
    private GetCourseStatisticsQueryHandler handler;

    private Course course;
    private Major majorCe;
    private Major majorEe;
    private Semester semester;
    private CourseOffering offering;

    @BeforeEach
    void setUp() {
        course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        ReflectionTestUtils.setField(course, "id", COURSE_ID);

        majorCe = new Major("Computer Engineering");
        ReflectionTestUtils.setField(majorCe, "id", MAJOR_CE_ID);

        majorEe = new Major("Electrical Engineering");
        ReflectionTestUtils.setField(majorEe, "id", MAJOR_EE_ID);

        semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        offering = new CourseOffering(course, semester, "Dr. Rezaei", "01", true, 30, "Sun 10:00", null);
    }

    @Test
    @DisplayName("Should calculate course statistics successfully across overall and majors")
    void shouldCalculateCourseStatisticsSuccessfully() {
        GetCourseStatisticsQuery query = new GetCourseStatisticsQuery(COURSE_ID);

        Student student1 = new Student("Ali", "401111", majorCe, EnrollmentPlanType.REGULAR);
        Student student2 = new Student("Sara", "401222", majorCe, EnrollmentPlanType.REGULAR);
        Student student3 = new Student("Reza", "401333", majorEe, EnrollmentPlanType.REGULAR);

        EnrollmentRecord r1 = new EnrollmentRecord(student1, offering);
        r1.assignGrade(GRADE_PASS);

        EnrollmentRecord r2 = new EnrollmentRecord(student2, offering);
        r2.assignGrade(GRADE_FAIL);

        EnrollmentRecord r3 = new EnrollmentRecord(student3, offering);

        List<EnrollmentRecord> records = List.of(r1, r2, r3);

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseOfferingRepository.countByCourseId(COURSE_ID)).thenReturn(TOTAL_OFFERINGS);
        when(enrollmentRecordRepository.findByCourseOfferingCourseId(COURSE_ID)).thenReturn(records);

        GetCourseStatisticsResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.overall().courseId()).isEqualTo(COURSE_ID);
        assertThat(response.overall().courseName()).isEqualTo(COURSE_NAME);
        assertThat(response.overall().courseCode()).isEqualTo(COURSE_CODE);
        assertThat(response.overall().totalOfferings()).isEqualTo(TOTAL_OFFERINGS);
        assertThat(response.overall().totalStudentsPassed()).isEqualTo(1);
        assertThat(response.overall().totalStudentsFailed()).isEqualTo(1);
        assertThat(response.overall().totalStudentsInProgress()).isEqualTo(1);
        assertThat(response.overall().averageGrade()).isEqualTo(EXPECTED_AVERAGE);

        assertThat(response.byMajor()).hasSize(2);

        GetCourseStatisticsResponse.MajorCourseStatistics ceStats = response.byMajor().stream()
                .filter(m -> m.majorId().equals(MAJOR_CE_ID))
                .findFirst()
                .orElseThrow();
        assertThat(ceStats.totalStudentsPassed()).isEqualTo(1);
        assertThat(ceStats.totalStudentsFailed()).isEqualTo(1);
        assertThat(ceStats.totalStudentsInProgress()).isEqualTo(0);
        assertThat(ceStats.averageGrade()).isEqualTo(EXPECTED_AVERAGE);

        GetCourseStatisticsResponse.MajorCourseStatistics eeStats = response.byMajor().stream()
                .filter(m -> m.majorId().equals(MAJOR_EE_ID))
                .findFirst()
                .orElseThrow();
        assertThat(eeStats.totalStudentsPassed()).isEqualTo(0);
        assertThat(eeStats.totalStudentsFailed()).isEqualTo(0);
        assertThat(eeStats.totalStudentsInProgress()).isEqualTo(1);
        assertThat(eeStats.averageGrade()).isEqualTo(ZERO_AVERAGE);

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should return zero average when no records exist")
    void shouldReturnZeroAverageWhenNoRecordsExist() {
        GetCourseStatisticsQuery query = new GetCourseStatisticsQuery(COURSE_ID);

        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(courseOfferingRepository.countByCourseId(COURSE_ID)).thenReturn(0);
        when(enrollmentRecordRepository.findByCourseOfferingCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        GetCourseStatisticsResponse response = handler.handle(query);

        assertThat(response.overall().totalOfferings()).isEqualTo(0);
        assertThat(response.overall().totalStudentsPassed()).isEqualTo(0);
        assertThat(response.overall().totalStudentsFailed()).isEqualTo(0);
        assertThat(response.overall().totalStudentsInProgress()).isEqualTo(0);
        assertThat(response.overall().averageGrade()).isEqualTo(ZERO_AVERAGE);
        assertThat(response.byMajor()).isEmpty();

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should throw CourseNotFoundException when course is not found")
    void shouldThrowExceptionWhenCourseNotFound() {
        GetCourseStatisticsQuery query = new GetCourseStatisticsQuery(COURSE_ID);
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(CourseNotFoundException.class);

        verify(validator).validate(query);
    }
}
