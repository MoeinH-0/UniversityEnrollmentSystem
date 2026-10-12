package com.example.universityenrollmentsystem.service.majors.addcourse;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.MajorNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddCourseToMajorCommandHandlerTest {

    private static final Long MAJOR_ID = 1L;
    private static final Long COURSE_ID = 10L;
    private static final int CREDITS = 3;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AddCourseToMajorCommandValidator validator;

    @InjectMocks
    private AddCourseToMajorCommandHandler handler;

    private Major major;
    private Course course;

    @BeforeEach
    void setUp() {
        major = new Major("Computer Engineering");
        course = new Course("Operating Systems", "CS401", CREDITS);
    }

    @Test
    @DisplayName("Should add course to major successfully")
    void shouldAddCourseToMajorSuccessfully() {
        AddCourseToMajorCommand command = new AddCourseToMajorCommand(MAJOR_ID, COURSE_ID);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.of(major));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        handler.handle(command);

        assertThat(major.getCourses()).contains(course);
        verify(validator).validate(command);
        verify(majorRepository).save(major);
    }

    @Test
    @DisplayName("Should throw MajorNotFoundException when major is not found")
    void shouldThrowExceptionWhenMajorNotFound() {
        AddCourseToMajorCommand command = new AddCourseToMajorCommand(MAJOR_ID, COURSE_ID);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(MajorNotFoundException.class);

        verify(validator).validate(command);
        verify(majorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CommandValidationException when major is archived")
    void shouldThrowExceptionWhenMajorIsArchived() {
        major.markAsDeleted();
        AddCourseToMajorCommand command = new AddCourseToMajorCommand(MAJOR_ID, COURSE_ID);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.of(major));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CommandValidationException.class);

        verify(validator).validate(command);
        verify(majorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CourseNotFoundException when course is not found")
    void shouldThrowExceptionWhenCourseNotFound() {
        AddCourseToMajorCommand command = new AddCourseToMajorCommand(MAJOR_ID, COURSE_ID);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.of(major));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseNotFoundException.class);

        verify(validator).validate(command);
        verify(majorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CommandValidationException when course is archived")
    void shouldThrowExceptionWhenCourseIsArchived() {
        course.markAsDeleted();
        AddCourseToMajorCommand command = new AddCourseToMajorCommand(MAJOR_ID, COURSE_ID);

        when(majorRepository.findById(MAJOR_ID)).thenReturn(Optional.of(major));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CommandValidationException.class);

        verify(validator).validate(command);
        verify(majorRepository, never()).save(any());
    }
}
