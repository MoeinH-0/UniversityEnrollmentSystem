package com.example.universityenrollmentsystem.service.courseOfferings.details;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
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
class GetCourseOfferingDetailsQueryHandlerTest {

    private static final Long OFFERING_ID = 100L;
    private static final Long COURSE_ID = 1L;
    private static final Long SEMESTER_ID = 10L;
    private static final String COURSE_NAME = "Computer Networks";
    private static final String COURSE_CODE = "CS402";
    private static final int CREDITS = 3;
    private static final String SEMESTER_TITLE = "Fall 2026";
    private static final String PROFESSOR = "Dr. Karimi";
    private static final String SECTION = "01";
    private static final boolean GUEST_ALLOWED = true;
    private static final int CAPACITY = 35;
    private static final String CLASS_TIME = "Mon 14:00-16:00";
    private static final LocalDate EXAM_DATE = LocalDate.of(2027, 1, 15);

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private EnrollmentRecordRepository enrollmentRecordRepository;

    @Mock
    private GetCourseOfferingDetailsQueryValidator validator;

    @InjectMocks
    private GetCourseOfferingDetailsQueryHandler handler;

    private Course course;
    private Semester semester;
    private CourseOffering offering;

    @BeforeEach
    void setUp() {
        course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        ReflectionTestUtils.setField(course, "id", COURSE_ID);

        semester = new Semester(SEMESTER_TITLE, LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        ReflectionTestUtils.setField(semester, "id", SEMESTER_ID);

        offering = new CourseOffering(course, semester, PROFESSOR, SECTION, GUEST_ALLOWED, CAPACITY, CLASS_TIME, EXAM_DATE);
        ReflectionTestUtils.setField(offering, "id", OFFERING_ID);
    }

    @Test
    @DisplayName("Should return course offering details successfully with enrolled count")
    void shouldReturnOfferingDetailsSuccessfully() {
        GetCourseOfferingDetailsQuery query = new GetCourseOfferingDetailsQuery(OFFERING_ID);

        Major major = new Major("Computer Engineering");
        Student student = new Student("Ali", "401111", major, EnrollmentPlanType.REGULAR);
        EnrollmentRecord record = new EnrollmentRecord(student, offering);

        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(enrollmentRecordRepository.findByCourseOfferingId(OFFERING_ID)).thenReturn(List.of(record));

        GetCourseOfferingDetailsResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.offeringId()).isEqualTo(OFFERING_ID);
        assertThat(response.courseId()).isEqualTo(COURSE_ID);
        assertThat(response.courseName()).isEqualTo(COURSE_NAME);
        assertThat(response.courseCode()).isEqualTo(COURSE_CODE);
        assertThat(response.credits()).isEqualTo(CREDITS);
        assertThat(response.semesterId()).isEqualTo(SEMESTER_ID);
        assertThat(response.semesterTitle()).isEqualTo(SEMESTER_TITLE);
        assertThat(response.professorName()).isEqualTo(PROFESSOR);
        assertThat(response.section()).isEqualTo(SECTION);
        assertThat(response.guestAllowed()).isEqualTo(GUEST_ALLOWED);
        assertThat(response.capacity()).isEqualTo(CAPACITY);
        assertThat(response.enrolledCount()).isEqualTo(1);
        assertThat(response.classTime()).isEqualTo(CLASS_TIME);
        assertThat(response.examDate()).isEqualTo(EXAM_DATE);
        assertThat(response.isDeleted()).isFalse();

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should throw CourseOfferingNotFoundException when offering is not found")
    void shouldThrowExceptionWhenOfferingNotFound() {
        GetCourseOfferingDetailsQuery query = new GetCourseOfferingDetailsQuery(OFFERING_ID);
        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(CourseOfferingNotFoundException.class);

        verify(validator).validate(query);
    }
}
