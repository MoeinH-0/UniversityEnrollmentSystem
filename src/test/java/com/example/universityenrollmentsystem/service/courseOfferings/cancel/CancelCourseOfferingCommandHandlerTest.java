package com.example.universityenrollmentsystem.service.courseOfferings.cancel;

import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
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
class CancelCourseOfferingCommandHandlerTest {

    private static final Long OFFERING_ID = 10L;
    private static final int COURSE_CREDITS = 3;
    private static final int CAPACITY = 30;

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private CancelCourseOfferingCommandValidator validator;

    @InjectMocks
    private CancelCourseOfferingCommandHandler handler;

    private CourseOffering courseOffering;

    @BeforeEach
    void setUp() {
        Course course = new Course("Computer Networks", "CS305", COURSE_CREDITS);
        Semester semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        courseOffering = new CourseOffering(course, semester, "Dr. Rezaei", "01", true, CAPACITY, "Sun 08:00", LocalDate.of(2027, 1, 12));
    }

    @Test
    @DisplayName("Should cancel course offering successfully and mark as deleted")
    void shouldCancelCourseOfferingSuccessfully() {
        CancelCourseOfferingCommand command = new CancelCourseOfferingCommand(OFFERING_ID);

        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(courseOffering));

        handler.handle(command);

        assertThat(courseOffering.isDeleted()).isTrue();
        verify(validator).validate(command);
        verify(courseOfferingRepository).save(courseOffering);
    }

    @Test
    @DisplayName("Should throw CourseOfferingNotFoundException when offering is not found")
    void shouldThrowExceptionWhenOfferingNotFound() {
        CancelCourseOfferingCommand command = new CancelCourseOfferingCommand(OFFERING_ID);

        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseOfferingNotFoundException.class);

        verify(validator).validate(command);
        verify(courseOfferingRepository, never()).save(any());
    }
}
