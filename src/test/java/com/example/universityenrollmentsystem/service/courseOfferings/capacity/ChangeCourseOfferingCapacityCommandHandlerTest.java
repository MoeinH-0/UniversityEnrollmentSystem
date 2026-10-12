package com.example.universityenrollmentsystem.service.courseOfferings.capacity;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeCourseOfferingCapacityCommandHandlerTest {

    private static final Long OFFERING_ID = 10L;
    private static final int INITIAL_CAPACITY = 30;
    private static final int HIGHER_CAPACITY = 40;
    private static final int EQUAL_CAPACITY = 2;
    private static final int LOWER_THAN_ENROLLED_CAPACITY = 1;
    private static final int COURSE_CREDITS = 3;

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private EnrollmentRecordRepository enrollmentRecordRepository;

    @Mock
    private ChangeCourseOfferingCapacityCommandValidator validator;

    @InjectMocks
    private ChangeCourseOfferingCapacityCommandHandler handler;

    private CourseOffering courseOffering;

    @BeforeEach
    void setUp() {
        Course course = new Course("Computer Networks", "CS305", COURSE_CREDITS);
        Semester semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        courseOffering = new CourseOffering(course, semester, "Dr. Rezaei", "01", true, INITIAL_CAPACITY, "Sun 08:00", LocalDate.of(2027, 1, 12));
    }

    @Test
    @DisplayName("Should change capacity successfully when new capacity is greater than enrolled count")
    void shouldChangeCapacitySuccessfullyWhenGreater() {
        ChangeCourseOfferingCapacityCommand command = new ChangeCourseOfferingCapacityCommand(OFFERING_ID, HIGHER_CAPACITY);

        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(courseOffering));
        when(enrollmentRecordRepository.findByCourseOfferingId(courseOffering.getId()))
                .thenReturn(List.of(mock(EnrollmentRecord.class), mock(EnrollmentRecord.class)));

        handler.handle(command);

        assertThat(courseOffering.getCapacity()).isEqualTo(HIGHER_CAPACITY);
        verify(validator).validate(command);
        verify(courseOfferingRepository).save(courseOffering);
    }

    @Test
    @DisplayName("Should change capacity successfully when new capacity equals enrolled count")
    void shouldChangeCapacitySuccessfullyWhenEqual() {
        ChangeCourseOfferingCapacityCommand command = new ChangeCourseOfferingCapacityCommand(OFFERING_ID, EQUAL_CAPACITY);

        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(courseOffering));
        when(enrollmentRecordRepository.findByCourseOfferingId(courseOffering.getId()))
                .thenReturn(List.of(mock(EnrollmentRecord.class), mock(EnrollmentRecord.class)));

        handler.handle(command);

        assertThat(courseOffering.getCapacity()).isEqualTo(EQUAL_CAPACITY);
        verify(validator).validate(command);
        verify(courseOfferingRepository).save(courseOffering);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when new capacity is strictly less than enrolled count")
    void shouldThrowExceptionWhenNewCapacityLessThanEnrolledCount() {
        ChangeCourseOfferingCapacityCommand command = new ChangeCourseOfferingCapacityCommand(OFFERING_ID, LOWER_THAN_ENROLLED_CAPACITY);

        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(courseOffering));
        when(enrollmentRecordRepository.findByCourseOfferingId(courseOffering.getId()))
                .thenReturn(List.of(mock(EnrollmentRecord.class), mock(EnrollmentRecord.class)));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CommandValidationException.class);

        verify(validator).validate(command);
        verify(courseOfferingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CourseOfferingNotFoundException when course offering is not found")
    void shouldThrowExceptionWhenOfferingNotFound() {
        ChangeCourseOfferingCapacityCommand command = new ChangeCourseOfferingCapacityCommand(OFFERING_ID, HIGHER_CAPACITY);

        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseOfferingNotFoundException.class);

        verify(validator).validate(command);
        verify(courseOfferingRepository, never()).save(any());
    }
}
