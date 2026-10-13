package com.example.universityenrollmentsystem.repository.student;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.course.CourseStatus;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EnrollmentRecordRepositoryTest {

    private static final String STUDENT_NUMBER_1 = "401111";
    private static final String STUDENT_NUMBER_2 = "401222";
    private static final Double PASSING_GRADE = 18.0;
    private static final Double FAILING_GRADE = 8.5;
    private static final Double MIN_GRADE = 0.0;
    private static final Double MAX_GRADE = 20.0;
    private static final Double HIGH_PRECISION_PASS = 19.75;
    private static final Double BORDER_FAIL_GRADE = 9.99;
    private static final Long NON_EXISTING_ID = 88888L;

    @Autowired
    private EnrollmentRecordRepository enrollmentRecordRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseOfferingRepository courseOfferingRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private MajorRepository majorRepository;

    private Student student1;
    private Student student2;
    private Course course;
    private CourseOffering offering1;
    private CourseOffering offering2;

    @BeforeEach
    void setUp() {
        Major major = new Major("Computer Engineering");
        majorRepository.saveAndFlush(major);

        student1 = new Student("Sara Ahmadi", STUDENT_NUMBER_1, major, EnrollmentPlanType.REGULAR);
        student2 = new Student("Reza Tehrani", STUDENT_NUMBER_2, major, EnrollmentPlanType.REGULAR);
        studentRepository.saveAndFlush(student1);
        studentRepository.saveAndFlush(student2);

        course = new Course("Computer Architecture", "CS303", 3);
        courseRepository.saveAndFlush(course);

        Semester semester1 = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));
        Semester semester2 = new Semester("Spring 2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 6, 20));
        semesterRepository.saveAndFlush(semester1);
        semesterRepository.saveAndFlush(semester2);

        offering1 = new CourseOffering(course, semester1, "Dr. Karimi", "01", true, 30, "Mon 10:00", null);
        offering2 = new CourseOffering(course, semester2, "Dr. Karimi", "01", true, 30, "Mon 10:00", null);
        courseOfferingRepository.saveAndFlush(offering1);
        courseOfferingRepository.saveAndFlush(offering2);
    }

    @Test
    @DisplayName("Should find enrollment record by studentId and courseOfferingId")
    void shouldFindRecordByStudentAndOffering() {
        EnrollmentRecord record = new EnrollmentRecord(student1, offering1);
        enrollmentRecordRepository.saveAndFlush(record);

        Optional<EnrollmentRecord> found = enrollmentRecordRepository
                .findByStudentIdAndCourseOfferingId(student1.getId(), offering1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStudent().getId()).isEqualTo(student1.getId());
        assertThat(found.get().getCourseOffering().getId()).isEqualTo(offering1.getId());
        assertThat(found.get().getStatus()).isEqualTo(CourseStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should return empty optional when student is not enrolled in offering")
    void shouldReturnEmptyWhenNotEnrolled() {
        Optional<EnrollmentRecord> found = enrollmentRecordRepository
                .findByStudentIdAndCourseOfferingId(student1.getId(), offering1.getId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all enrollment records for a specific offering")
    void shouldFindRecordsByCourseOfferingId() {
        EnrollmentRecord record1 = new EnrollmentRecord(student1, offering1);
        EnrollmentRecord record2 = new EnrollmentRecord(student2, offering1);
        enrollmentRecordRepository.saveAndFlush(record1);
        enrollmentRecordRepository.saveAndFlush(record2);

        List<EnrollmentRecord> records = enrollmentRecordRepository.findByCourseOfferingId(offering1.getId());

        assertThat(records).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when course offering has no enrolled students")
    void shouldReturnEmptyListWhenOfferingHasNoEnrolledStudents() {
        List<EnrollmentRecord> records = enrollmentRecordRepository.findByCourseOfferingId(offering2.getId());

        assertThat(records).isNotNull();
        assertThat(records).isEmpty();
    }

    @Test
    @DisplayName("Should find all enrollment records across all offerings of a course")
    void shouldFindRecordsByCourseOfferingCourseId() {
        EnrollmentRecord record1 = new EnrollmentRecord(student1, offering1);
        EnrollmentRecord record2 = new EnrollmentRecord(student2, offering2);
        enrollmentRecordRepository.saveAndFlush(record1);
        enrollmentRecordRepository.saveAndFlush(record2);

        List<EnrollmentRecord> records = enrollmentRecordRepository.findByCourseOfferingCourseId(course.getId());

        assertThat(records).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty list when course has no enrollment records")
    void shouldReturnEmptyWhenCourseHasNoEnrollments() {
        List<EnrollmentRecord> records = enrollmentRecordRepository.findByCourseOfferingCourseId(NON_EXISTING_ID);

        assertThat(records).isEmpty();
    }

    @Test
    @DisplayName("Should persist grade assignment and status transition to PASSED")
    void shouldPersistPassingGrade() {
        EnrollmentRecord record = new EnrollmentRecord(student1, offering1);
        EnrollmentRecord saved = enrollmentRecordRepository.saveAndFlush(record);

        saved.assignGrade(PASSING_GRADE);
        enrollmentRecordRepository.saveAndFlush(saved);

        Optional<EnrollmentRecord> reloaded = enrollmentRecordRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getGrade()).isEqualTo(PASSING_GRADE);
        assertThat(reloaded.get().getStatus()).isEqualTo(CourseStatus.PASSED);
    }

    @Test
    @DisplayName("Should persist failing grade and status transition to FAILED")
    void shouldPersistFailingGrade() {
        EnrollmentRecord record = new EnrollmentRecord(student1, offering1);
        EnrollmentRecord saved = enrollmentRecordRepository.saveAndFlush(record);

        saved.assignGrade(FAILING_GRADE);
        enrollmentRecordRepository.saveAndFlush(saved);

        Optional<EnrollmentRecord> reloaded = enrollmentRecordRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getGrade()).isEqualTo(FAILING_GRADE);
        assertThat(reloaded.get().getStatus()).isEqualTo(CourseStatus.FAILED);
    }

    @Test
    @DisplayName("Should preserve exact decimal precision for boundary grades (0.0, 20.0, 19.75, 9.99)")
    void shouldPreserveExactDecimalPrecisionForBoundaryGrades() {
        EnrollmentRecord recordMin = new EnrollmentRecord(student1, offering1);
        EnrollmentRecord recordMax = new EnrollmentRecord(student2, offering1);
        EnrollmentRecord savedMin = enrollmentRecordRepository.saveAndFlush(recordMin);
        EnrollmentRecord savedMax = enrollmentRecordRepository.saveAndFlush(recordMax);

        savedMin.assignGrade(MIN_GRADE);
        savedMax.assignGrade(MAX_GRADE);
        enrollmentRecordRepository.saveAndFlush(savedMin);
        enrollmentRecordRepository.saveAndFlush(savedMax);

        Optional<EnrollmentRecord> loadedMin = enrollmentRecordRepository.findById(savedMin.getId());
        Optional<EnrollmentRecord> loadedMax = enrollmentRecordRepository.findById(savedMax.getId());

        assertThat(loadedMin).isPresent();
        assertThat(loadedMin.get().getGrade()).isEqualTo(MIN_GRADE);
        assertThat(loadedMin.get().getStatus()).isEqualTo(CourseStatus.FAILED);

        assertThat(loadedMax).isPresent();
        assertThat(loadedMax.get().getGrade()).isEqualTo(MAX_GRADE);
        assertThat(loadedMax.get().getStatus()).isEqualTo(CourseStatus.PASSED);

        EnrollmentRecord recordPrecise = new EnrollmentRecord(student1, offering2);
        EnrollmentRecord recordBorder = new EnrollmentRecord(student2, offering2);
        EnrollmentRecord savedPrecise = enrollmentRecordRepository.saveAndFlush(recordPrecise);
        EnrollmentRecord savedBorder = enrollmentRecordRepository.saveAndFlush(recordBorder);

        savedPrecise.assignGrade(HIGH_PRECISION_PASS);
        savedBorder.assignGrade(BORDER_FAIL_GRADE);
        enrollmentRecordRepository.saveAndFlush(savedPrecise);
        enrollmentRecordRepository.saveAndFlush(savedBorder);

        Optional<EnrollmentRecord> loadedPrecise = enrollmentRecordRepository.findById(savedPrecise.getId());
        Optional<EnrollmentRecord> loadedBorder = enrollmentRecordRepository.findById(savedBorder.getId());

        assertThat(loadedPrecise).isPresent();
        assertThat(loadedPrecise.get().getGrade()).isEqualTo(HIGH_PRECISION_PASS);
        assertThat(loadedPrecise.get().getStatus()).isEqualTo(CourseStatus.PASSED);

        assertThat(loadedBorder).isPresent();
        assertThat(loadedBorder.get().getGrade()).isEqualTo(BORDER_FAIL_GRADE);
        assertThat(loadedBorder.get().getStatus()).isEqualTo(CourseStatus.FAILED);
    }

    @Test
    @DisplayName("Should persist status transition when marking as passed without grade")
    void shouldPersistMarkAsPassedWithoutGrade() {
        EnrollmentRecord record = new EnrollmentRecord(student1, offering1);
        EnrollmentRecord saved = enrollmentRecordRepository.saveAndFlush(record);

        saved.markAsPassed();
        enrollmentRecordRepository.saveAndFlush(saved);

        Optional<EnrollmentRecord> reloaded = enrollmentRecordRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getStatus()).isEqualTo(CourseStatus.PASSED);
    }

    @Test
    @DisplayName("Should persist status transition when marking as failed without grade")
    void shouldPersistMarkAsFailedWithoutGrade() {
        EnrollmentRecord record = new EnrollmentRecord(student1, offering1);
        EnrollmentRecord saved = enrollmentRecordRepository.saveAndFlush(record);

        saved.markAsFailed();
        enrollmentRecordRepository.saveAndFlush(saved);

        Optional<EnrollmentRecord> reloaded = enrollmentRecordRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getStatus()).isEqualTo(CourseStatus.FAILED);
    }

    @Test
    @DisplayName("Should delete enrollment record successfully from database")
    void shouldDeleteEnrollmentRecord() {
        EnrollmentRecord record = new EnrollmentRecord(student1, offering1);
        EnrollmentRecord saved = enrollmentRecordRepository.saveAndFlush(record);

        enrollmentRecordRepository.delete(saved);
        enrollmentRecordRepository.flush();

        Optional<EnrollmentRecord> found = enrollmentRecordRepository.findById(saved.getId());

        assertThat(found).isEmpty();
    }
}
