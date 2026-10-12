package com.example.universityenrollmentsystem.service.courseOfferings.grade;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.student.EnrollmentRecordRepository;
import com.example.universityenrollmentsystem.service.exceptions.notfound.EnrollmentRecordNotFoundException;
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
class SubmitStudentGradeCommandHandlerTest {

    private static final Long STUDENT_ID = 1L;
    private static final Long OFFERING_ID = 10L;
    private static final Double PASSING_GRADE = 16.5;
    private static final Double FAILING_GRADE = 8.0;
    private static final int COURSE_CREDITS = 3;
    private static final int OFFERING_CAPACITY = 30;

    @Mock
    private EnrollmentRecordRepository enrollmentRecordRepository;

    @Mock
    private SubmitStudentGradeCommandValidator validator;

    @InjectMocks
    private SubmitStudentGradeCommandHandler handler;

    private EnrollmentRecord enrollmentRecord;

    @BeforeEach
    void setUp() {
        Major major = new Major("Computer Engineering");
        Course course = new Course("Data Structures", "CS201", COURSE_CREDITS);
        Semester semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        Student student = new Student("Ali Rezaei", "401123456", major, EnrollmentPlanType.REGULAR);
        CourseOffering offering = new CourseOffering(course, semester, "Dr. Ahmadi", "01", true, OFFERING_CAPACITY, "Mon 10:00", LocalDate.of(2027, 1, 10));

        enrollmentRecord = new EnrollmentRecord(student, offering);
    }

    @Test
    @DisplayName("Should submit passing grade successfully and update status to PASSED")
    void shouldSubmitPassingGradeSuccessfully() {
        SubmitStudentGradeCommand command = new SubmitStudentGradeCommand(STUDENT_ID, OFFERING_ID, PASSING_GRADE);

        when(enrollmentRecordRepository.findByStudentIdAndCourseOfferingId(STUDENT_ID, OFFERING_ID))
                .thenReturn(Optional.of(enrollmentRecord));

        handler.handle(command);

        assertThat(enrollmentRecord.getGrade()).isEqualTo(PASSING_GRADE);
        assertThat(enrollmentRecord.getStatus()).isEqualTo(CourseStatus.PASSED);

        verify(validator).validate(command);
        verify(enrollmentRecordRepository).save(enrollmentRecord);
    }

    @Test
    @DisplayName("Should submit failing grade successfully and update status to FAILED")
    void shouldSubmitFailingGradeSuccessfully() {
        SubmitStudentGradeCommand command = new SubmitStudentGradeCommand(STUDENT_ID, OFFERING_ID, FAILING_GRADE);

        when(enrollmentRecordRepository.findByStudentIdAndCourseOfferingId(STUDENT_ID, OFFERING_ID))
                .thenReturn(Optional.of(enrollmentRecord));

        handler.handle(command);

        assertThat(enrollmentRecord.getGrade()).isEqualTo(FAILING_GRADE);
        assertThat(enrollmentRecord.getStatus()).isEqualTo(CourseStatus.FAILED);

        verify(validator).validate(command);
        verify(enrollmentRecordRepository).save(enrollmentRecord);
    }

    @Test
    @DisplayName("Should throw EnrollmentRecordNotFoundException when enrollment record is not found")
    void shouldThrowExceptionWhenRecordNotFound() {
        SubmitStudentGradeCommand command = new SubmitStudentGradeCommand(STUDENT_ID, OFFERING_ID, PASSING_GRADE);

        when(enrollmentRecordRepository.findByStudentIdAndCourseOfferingId(STUDENT_ID, OFFERING_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(EnrollmentRecordNotFoundException.class);

        verify(validator).validate(command);
        verify(enrollmentRecordRepository, never()).save(any());
    }
}
