package com.example.universityenrollmentsystem.service.enrollments.drop;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.exception.state.InvalidEnrollmentStateException;
import com.example.universityenrollmentsystem.domain.service.enrollment.EnrollmentPlanStrategy;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DropCourseOfferingCommandHandlerTest {

    private static final Long STUDENT_ID = 1L;
    private static final Long OFFERING_ID = 10L;

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DropCourseOfferingCommandValidator validator;

    @InjectMocks
    private DropCourseOfferingCommandHandler handler;

    private Student student;
    private CourseOffering offering;

    @BeforeEach
    void setUp() {
        Major major = new Major("Computer Science");
        Semester semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        Course course = new Course("Data Structures", "CS201", 3);
        major.addCourse(course);

        student = new Student("Ali Rezaei", "401001", major, EnrollmentPlanType.REGULAR);
        offering = new CourseOffering(course, semester, "Dr. Alavi", "01", true, 30, "Mon 10:00", semester.getEndDate().minusWeeks(2));
    }

    @Test
    @DisplayName("Should successfully drop enrolled course offering")
    void shouldSuccessfullyDropCourseOffering() {
        EnrollmentPlanStrategy mockStrategy = mock(EnrollmentPlanStrategy.class);
        student.enroll(List.of(offering), mockStrategy);

        DropCourseOfferingCommand command = new DropCourseOfferingCommand(OFFERING_ID, STUDENT_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));

        handler.handle(command);

        verify(validator).validate(command);
        verify(studentRepository).save(student);
        assertThat(student.getEnrollments()).isEmpty();
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when student does not exist")
    void shouldThrowExceptionWhenStudentNotFound() {
        DropCourseOfferingCommand command = new DropCourseOfferingCommand(OFFERING_ID, STUDENT_ID);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(StudentNotFoundException.class);

        verify(validator).validate(command);
        verifyNoInteractions(courseOfferingRepository);
    }

    @Test
    @DisplayName("Should throw CourseOfferingNotFoundException when course offering does not exist")
    void shouldThrowExceptionWhenOfferingNotFound() {
        DropCourseOfferingCommand command = new DropCourseOfferingCommand(OFFERING_ID, STUDENT_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseOfferingNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw InvalidEnrollmentStateException when student is not enrolled in the course offering")
    void shouldThrowExceptionWhenStudentNotEnrolled() {
        DropCourseOfferingCommand command = new DropCourseOfferingCommand(OFFERING_ID, STUDENT_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InvalidEnrollmentStateException.class);

        verify(studentRepository, never()).save(any());
    }
}
