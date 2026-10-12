package com.example.universityenrollmentsystem.service.courseOfferings.exam;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.exception.validation.DomainValidationException;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RescheduleCourseOfferingExamCommandHandlerTest {

    private static final Long OFFERING_ID = 10L;
    private static final int COURSE_CREDITS = 3;
    private static final int CAPACITY = 30;
    private static final LocalDate SEMESTER_START = LocalDate.of(2026, 9, 23);
    private static final LocalDate SEMESTER_END = LocalDate.of(2027, 1, 20);
    private static final LocalDate VALID_EXAM_DATE = LocalDate.of(2027, 1, 15);
    private static final LocalDate DATE_BEFORE_SEMESTER = LocalDate.of(2026, 9, 1);
    private static final LocalDate DATE_AFTER_SEMESTER = LocalDate.of(2027, 2, 1);

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private RescheduleCourseOfferingExamCommandValidator validator;

    @InjectMocks
    private RescheduleCourseOfferingExamCommandHandler handler;

    private CourseOffering courseOffering;

    @BeforeEach
    void setUp() {
        Course course = new Course("Algorithms", "CS302", COURSE_CREDITS);
        Semester semester = new Semester("Fall 2026", SEMESTER_START, SEMESTER_END);
        courseOffering = new CourseOffering(course, semester, "Dr. Ahmadi", "01", true, CAPACITY, "Tue 10:00", null);
    }

    @Test
    @DisplayName("Should reschedule exam date successfully when date is within semester dates")
    void shouldRescheduleExamDateSuccessfully() {
        RescheduleCourseOfferingExamCommand command = new RescheduleCourseOfferingExamCommand(OFFERING_ID, VALID_EXAM_DATE);

        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(courseOffering));

        handler.handle(command);

        assertThat(courseOffering.getExamDate()).isEqualTo(VALID_EXAM_DATE);
        verify(validator).validate(command);
        verify(courseOfferingRepository).save(courseOffering);
    }

    @Test
    @DisplayName("Should throw CourseOfferingNotFoundException when offering is not found")
    void shouldThrowExceptionWhenOfferingNotFound() {
        RescheduleCourseOfferingExamCommand command = new RescheduleCourseOfferingExamCommand(OFFERING_ID, VALID_EXAM_DATE);

        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseOfferingNotFoundException.class);

        verify(validator).validate(command);
        verify(courseOfferingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DomainValidationException when exam date is outside semester bounds")
    void shouldThrowExceptionWhenDateOutsideSemester() {
        RescheduleCourseOfferingExamCommand beforeCmd = new RescheduleCourseOfferingExamCommand(OFFERING_ID, DATE_BEFORE_SEMESTER);
        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(courseOffering));

        assertThatThrownBy(() -> handler.handle(beforeCmd))
                .isInstanceOf(DomainValidationException.class);

        RescheduleCourseOfferingExamCommand afterCmd = new RescheduleCourseOfferingExamCommand(OFFERING_ID, DATE_AFTER_SEMESTER);

        assertThatThrownBy(() -> handler.handle(afterCmd))
                .isInstanceOf(DomainValidationException.class);

        verify(courseOfferingRepository, never()).save(any());
    }
}
