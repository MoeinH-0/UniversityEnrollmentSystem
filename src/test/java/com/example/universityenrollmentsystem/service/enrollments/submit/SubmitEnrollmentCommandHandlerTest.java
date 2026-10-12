package com.example.universityenrollmentsystem.service.enrollments.submit;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import com.example.universityenrollmentsystem.domain.service.enrollment.EnrollmentPlanFactory;
import com.example.universityenrollmentsystem.domain.service.enrollment.EnrollmentPlanStrategy;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import com.example.universityenrollmentsystem.repository.student.StudentRepository;
import com.example.universityenrollmentsystem.service.exceptions.conflict.CourseOfferingFullException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.CourseOfferingNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.notfound.StudentNotFoundException;
import com.example.universityenrollmentsystem.service.exceptions.validation.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitEnrollmentCommandHandlerTest {

    private static final Long STUDENT_ID = 1L;
    private static final Long OFFERING_ID = 10L;
    private static final int CAPACITY = 30;

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRecordRepository enrollmentRecordRepository;

    @Mock
    private EnrollmentPlanFactory enrollmentPlanFactory;

    @Mock
    private SubmitEnrollmentCommandValidator validator;

    @Mock
    private EnrollmentPlanStrategy strategy;

    @InjectMocks
    private SubmitEnrollmentCommandHandler handler;

    private Student student;
    private CourseOffering offering;

    @BeforeEach
    void setUp() {
        Major major = new Major("Computer Science");
        Semester semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        Course course = new Course("Data Structures", "CS201", 3);
        major.addCourse(course);

        student = new Student("Ali Rezaei", "401001", major, EnrollmentPlanType.REGULAR);
        offering = new CourseOffering(course, semester, "Dr. Alavi", "01", true, CAPACITY, "Mon 10:00", semester.getEndDate().minusWeeks(2));
    }

    @Test
    @DisplayName("Should successfully enroll active student when offering has capacity")
    void shouldSuccessfullyEnrollStudent() {
        SubmitEnrollmentCommand command = new SubmitEnrollmentCommand(STUDENT_ID, OFFERING_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(enrollmentRecordRepository.findByCourseOfferingId(offering.getId())).thenReturn(List.of());
        when(enrollmentPlanFactory.getStrategy(EnrollmentPlanType.REGULAR)).thenReturn(strategy);

        handler.handle(command);

        verify(validator).validate(command);
        verify(strategy).validate(student, List.of(offering));
        verify(studentRepository).save(student);
        assertThat(student.getEnrollments()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when student does not exist")
    void shouldThrowExceptionWhenStudentNotFound() {
        SubmitEnrollmentCommand command = new SubmitEnrollmentCommand(STUDENT_ID, OFFERING_ID);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(StudentNotFoundException.class);

        verify(validator).validate(command);
        verifyNoInteractions(courseOfferingRepository, enrollmentRecordRepository, enrollmentPlanFactory);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when student is not ACTIVE")
    void shouldThrowExceptionWhenStudentNotActive() {
        SubmitEnrollmentCommand command = new SubmitEnrollmentCommand(STUDENT_ID, OFFERING_ID);
        student.updateStatus(StudentStatus.WITHDRAWN);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CommandValidationException.class)
                .hasMessageContaining("Only active students can enroll in courses");
    }

    @Test
    @DisplayName("Should throw CourseOfferingNotFoundException when course offering does not exist")
    void shouldThrowExceptionWhenOfferingNotFound() {
        SubmitEnrollmentCommand command = new SubmitEnrollmentCommand(STUDENT_ID, OFFERING_ID);

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseOfferingNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw CommandValidationException when course offering is deleted")
    void shouldThrowExceptionWhenOfferingIsDeleted() {
        SubmitEnrollmentCommand command = new SubmitEnrollmentCommand(STUDENT_ID, OFFERING_ID);
        offering.markAsDeleted();

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CommandValidationException.class)
                .hasMessageContaining("Cannot enroll in a cancelled or deleted course offering");
    }

    @Test
    @DisplayName("Should throw CourseOfferingFullException when capacity is reached")
    void shouldThrowExceptionWhenOfferingIsFull() {
        SubmitEnrollmentCommand command = new SubmitEnrollmentCommand(STUDENT_ID, OFFERING_ID);

        List<EnrollmentRecord> fullList = new ArrayList<>();
        for (int i = 0; i < CAPACITY; i++)
            fullList.add(mock(EnrollmentRecord.class));

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(courseOfferingRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(enrollmentRecordRepository.findByCourseOfferingId(offering.getId())).thenReturn(fullList);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(CourseOfferingFullException.class);

        verify(studentRepository, never()).save(any());
    }
}
