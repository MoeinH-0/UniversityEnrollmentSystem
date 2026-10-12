package com.example.universityenrollmentsystem.service.enrollments.eligible;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
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
class GetEligibleCoursesQueryHandlerTest {

    private static final Long STUDENT_ID = 1L;
    private static final Long SEMESTER_ID = 10L;
    private static final Long OFFERING1_ID = 100L;
    private static final Long OFFERING2_ID = 200L;
    private static final Long OFFERING3_ID = 300L;
    private static final Long COURSE1_ID = 11L;
    private static final String COURSE1_NAME = "Computer Graphics";
    private static final String COURSE1_CODE = "CS420";
    private static final int COURSE1_CREDITS = 3;
    private static final String PROFESSOR = "Dr. Jalili";
    private static final String SECTION = "01";
    private static final int CAPACITY = 30;
    private static final String CLASS_TIME = "Sun 08:00-10:00";
    private static final LocalDate EXAM_DATE = LocalDate.of(2027, 1, 20);

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private GetEligibleCoursesQueryValidator validator;

    @Mock
    private CourseEligibilityChecker eligibilityChecker;

    @InjectMocks
    private GetEligibleCoursesQueryHandler handler;

    private Student student;
    private Semester semester;
    private CourseOffering eligibleOffering;
    private CourseOffering ineligibleOffering;
    private CourseOffering deletedOffering;

    @BeforeEach
    void setUp() {
        Major major = new Major("Computer Engineering");
        student = new Student("Kasra Alavi", "401123456", major, EnrollmentPlanType.REGULAR);
        ReflectionTestUtils.setField(student, "id", STUDENT_ID);

        semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        ReflectionTestUtils.setField(semester, "id", SEMESTER_ID);

        Course course1 = new Course(COURSE1_NAME, COURSE1_CODE, COURSE1_CREDITS);
        ReflectionTestUtils.setField(course1, "id", COURSE1_ID);

        eligibleOffering = new CourseOffering(course1, semester, PROFESSOR, SECTION, true, CAPACITY, CLASS_TIME, EXAM_DATE);
        ReflectionTestUtils.setField(eligibleOffering, "id", OFFERING1_ID);

        ineligibleOffering = new CourseOffering(course1, semester, PROFESSOR, "02", true, CAPACITY, CLASS_TIME, EXAM_DATE);
        ReflectionTestUtils.setField(ineligibleOffering, "id", OFFERING2_ID);

        deletedOffering = new CourseOffering(course1, semester, PROFESSOR, "03", true, CAPACITY, CLASS_TIME, EXAM_DATE);
        ReflectionTestUtils.setField(deletedOffering, "id", OFFERING3_ID);
        deletedOffering.markAsDeleted();
    }

    @Test
    @DisplayName("Should return eligible offerings filtered by checker and excluding deleted")
    void shouldReturnEligibleOfferingsSuccessfully() {
        GetEligibleCoursesQuery query = new GetEligibleCoursesQuery(STUDENT_ID, SEMESTER_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(courseOfferingRepository.findBySemesterId(SEMESTER_ID))
                .thenReturn(List.of(eligibleOffering, ineligibleOffering, deletedOffering));

        when(eligibilityChecker.isEligible(student, eligibleOffering)).thenReturn(true);
        when(eligibilityChecker.isEligible(student, ineligibleOffering)).thenReturn(false);

        GetEligibleCoursesResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.studentId()).isEqualTo(STUDENT_ID);
        assertThat(response.semesterId()).isEqualTo(SEMESTER_ID);
        assertThat(response.eligibleOfferings()).hasSize(1);

        GetEligibleCoursesResponse.EligibleCourseOfferingDto dto = response.eligibleOfferings().get(0);
        assertThat(dto.offeringId()).isEqualTo(OFFERING1_ID);
        assertThat(dto.courseId()).isEqualTo(COURSE1_ID);
        assertThat(dto.courseName()).isEqualTo(COURSE1_NAME);
        assertThat(dto.courseCode()).isEqualTo(COURSE1_CODE);
        assertThat(dto.credits()).isEqualTo(COURSE1_CREDITS);
        assertThat(dto.professorName()).isEqualTo(PROFESSOR);
        assertThat(dto.section()).isEqualTo(SECTION);
        assertThat(dto.capacity()).isEqualTo(CAPACITY);
        assertThat(dto.classTime()).isEqualTo(CLASS_TIME);
        assertThat(dto.examDate()).isEqualTo(EXAM_DATE);
        assertThat(dto.guestAllowed()).isTrue();

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when student is not found")
    void shouldThrowExceptionWhenStudentNotFound() {
        GetEligibleCoursesQuery query = new GetEligibleCoursesQuery(STUDENT_ID, SEMESTER_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(StudentNotFoundException.class);

        verify(validator).validate(query);
    }

    @Test
    @DisplayName("Should throw SemesterNotFoundException when semester is not found")
    void shouldThrowExceptionWhenSemesterNotFound() {
        GetEligibleCoursesQuery query = new GetEligibleCoursesQuery(STUDENT_ID, SEMESTER_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(SemesterNotFoundException.class);

        verify(validator).validate(query);
    }
}
