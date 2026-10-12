package com.example.universityenrollmentsystem.service.semesters.offerings;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.SemesterNotFoundException;
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
class GetSemesterCourseOfferingsQueryHandlerTest {

    private static final Long SEMESTER_ID = 1L;
    private static final String SEMESTER_TITLE = "Spring 2027";
    private static final Long OFFERING1_ID = 10L;
    private static final Long OFFERING2_ID = 20L;
    private static final Long COURSE1_ID = 100L;
    private static final String COURSE1_NAME = "Artificial Intelligence";
    private static final String COURSE1_CODE = "AI401";
    private static final int COURSE1_CREDITS = 3;
    private static final String PROFESSOR = "Dr. Hassani";
    private static final String SECTION = "01";
    private static final int CAPACITY = 25;
    private static final String CLASS_TIME = "Tue 10:00-12:00";
    private static final LocalDate EXAM_DATE = LocalDate.of(2027, 6, 15);

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private GetSemesterCourseOfferingsQueryValidator validator;

    @InjectMocks
    private GetSemesterCourseOfferingsQueryHandler handler;

    private Semester semester;
    private CourseOffering activeOffering;
    private CourseOffering deletedOffering;

    @BeforeEach
    void setUp() {
        semester = new Semester(SEMESTER_TITLE, LocalDate.of(2027, 2, 1), LocalDate.of(2027, 6, 20));
        ReflectionTestUtils.setField(semester, "id", SEMESTER_ID);

        Course course1 = new Course(COURSE1_NAME, COURSE1_CODE, COURSE1_CREDITS);
        ReflectionTestUtils.setField(course1, "id", COURSE1_ID);

        activeOffering = new CourseOffering(course1, semester, PROFESSOR, SECTION, true, CAPACITY, CLASS_TIME, EXAM_DATE);
        ReflectionTestUtils.setField(activeOffering, "id", OFFERING1_ID);

        deletedOffering = new CourseOffering(course1, semester, PROFESSOR, "02", false, CAPACITY, CLASS_TIME, EXAM_DATE);
        ReflectionTestUtils.setField(deletedOffering, "id", OFFERING2_ID);
        deletedOffering.markAsDeleted();
    }

    @Test
    @DisplayName("Should return semester course offerings excluding deleted offerings")
    void shouldReturnOfferingsExcludingDeleted() {
        GetSemesterCourseOfferingsQuery query = new GetSemesterCourseOfferingsQuery(SEMESTER_ID);

        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.of(semester));
        when(courseOfferingRepository.findBySemesterId(SEMESTER_ID))
                .thenReturn(List.of(activeOffering, deletedOffering));

        GetSemesterCourseOfferingsResponse response = handler.handle(query);

        assertThat(response).isNotNull();
        assertThat(response.semesterId()).isEqualTo(SEMESTER_ID);
        assertThat(response.semesterTitle()).isEqualTo(SEMESTER_TITLE);
        assertThat(response.offerings()).hasSize(1);

        GetSemesterCourseOfferingsResponse.SemesterCourseOfferingDto dto = response.offerings().get(0);
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
    @DisplayName("Should throw SemesterNotFoundException when semester is not found")
    void shouldThrowExceptionWhenSemesterNotFound() {
        GetSemesterCourseOfferingsQuery query = new GetSemesterCourseOfferingsQuery(SEMESTER_ID);
        when(semesterRepository.findById(SEMESTER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(SemesterNotFoundException.class);

        verify(validator).validate(query);
    }
}
