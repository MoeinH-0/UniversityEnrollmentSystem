package com.example.universityenrollmentsystem.repository.student;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import com.example.universityenrollmentsystem.domain.service.enrollment.RegularEnrollmentPlanStrategy;
import com.example.universityenrollmentsystem.repository.academic.MajorRepository;
import com.example.universityenrollmentsystem.repository.academic.SemesterRepository;
import com.example.universityenrollmentsystem.repository.course.CourseOfferingRepository;
import com.example.universityenrollmentsystem.repository.course.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class StudentRepositoryTest {

    private static final String FULL_NAME = "Ali Ahmadi";
    private static final String UPDATED_FULL_NAME = "Ali Ahmadi Rad";
    private static final String STUDENT_NUMBER = "401123456";
    private static final String NON_EXISTING_NUMBER = "999999999";
    private static final String OTHER_STUDENT_NUMBER = "401987654";
    private static final String EMAIL = "ali@uni.edu";
    private static final String PHONE = "09121112233";
    private static final String COURSE_NAME = "Algorithms";
    private static final String COURSE_CODE = "CS302";
    private static final int CREDITS = 3;
    private static final String PROFESSOR = "Dr. Karimi";
    private static final String SECTION = "01";
    private static final int CAPACITY = 30;
    private static final String CLASS_TIME = "Mon 10:00";
    private static final String SEMESTER_TITLE = "Fall 2026";
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 23);
    private static final LocalDate END_DATE = LocalDate.of(2027, 1, 20);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private CourseOfferingRepository courseOfferingRepository;

    @Autowired
    private EnrollmentRecordRepository enrollmentRecordRepository;

    private Major major1;
    private Major major2;

    @BeforeEach
    void setUp() {
        major1 = new Major("Computer Engineering");
        majorRepository.saveAndFlush(major1);

        major2 = new Major("Electrical Engineering");
        majorRepository.saveAndFlush(major2);
    }

    @Test
    @DisplayName("Should return true when student number exists in database")
    void shouldReturnTrueWhenStudentNumberExists() {
        Student student = new Student(FULL_NAME, STUDENT_NUMBER, major1, EnrollmentPlanType.REGULAR);
        studentRepository.saveAndFlush(student);

        boolean exists = studentRepository.existsByStudentNumber(STUDENT_NUMBER);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when student number does not exist in database")
    void shouldReturnFalseWhenStudentNumberDoesNotExist() {
        boolean exists = studentRepository.existsByStudentNumber(NON_EXISTING_NUMBER);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when inserting duplicate student number")
    void shouldThrowExceptionWhenInsertingDuplicateStudentNumber() {
        Student student1 = new Student(FULL_NAME, STUDENT_NUMBER, major1, EnrollmentPlanType.REGULAR);
        studentRepository.saveAndFlush(student1);

        Student student2 = new Student("Another Name", STUDENT_NUMBER, major2, EnrollmentPlanType.PROBATION);

        assertThatThrownBy(() -> studentRepository.saveAndFlush(student2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should find students belonging to a specific major")
    void shouldFindStudentsByMajorId() {
        Student s1 = new Student("Student One", STUDENT_NUMBER, major1, EnrollmentPlanType.REGULAR);
        Student s2 = new Student("Student Two", OTHER_STUDENT_NUMBER, major1, EnrollmentPlanType.HONORS);
        Student s3 = new Student("Student Three", "401555666", major2, EnrollmentPlanType.GUEST);

        studentRepository.saveAndFlush(s1);
        studentRepository.saveAndFlush(s2);
        studentRepository.saveAndFlush(s3);

        List<Student> major1Students = studentRepository.findByMajorId(major1.getId());

        assertThat(major1Students).hasSize(2);
        assertThat(major1Students).extracting(Student::getStudentNumber).containsExactlyInAnyOrder(STUDENT_NUMBER, OTHER_STUDENT_NUMBER);
    }

    @Test
    @DisplayName("Should return empty list when no students belong to major")
    void shouldReturnEmptyListWhenNoStudentsInMajor() {
        List<Student> students = studentRepository.findByMajorId(major2.getId());

        assertThat(students).isEmpty();
    }

    @Test
    @DisplayName("Should persist student status and contact info modifications correctly")
    void shouldPersistStudentModifications() {
        Student student = new Student(FULL_NAME, STUDENT_NUMBER, major1, EnrollmentPlanType.REGULAR);
        Student saved = studentRepository.saveAndFlush(student);

        saved.updateContactInfo(EMAIL, PHONE);
        saved.updateStatus(StudentStatus.WITHDRAWN);
        saved.updateMajor(major2);
        studentRepository.saveAndFlush(saved);

        Optional<Student> reloaded = studentRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getEmail()).isEqualTo(EMAIL);
        assertThat(reloaded.get().getPhoneNumber()).isEqualTo(PHONE);
        assertThat(reloaded.get().getStatus()).isEqualTo(StudentStatus.WITHDRAWN);
        assertThat(reloaded.get().getMajor().getId()).isEqualTo(major2.getId());
    }

    @Test
    @DisplayName("Should persist student plan type and full name modifications correctly")
    void shouldPersistPlanTypeAndFullNameModifications() {
        Student student = new Student(FULL_NAME, STUDENT_NUMBER, major1, EnrollmentPlanType.REGULAR);
        Student saved = studentRepository.saveAndFlush(student);

        saved.updateFullName(UPDATED_FULL_NAME);
        saved.updatePlanType(EnrollmentPlanType.HONORS);
        studentRepository.saveAndFlush(saved);

        Optional<Student> reloaded = studentRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getFullName()).isEqualTo(UPDATED_FULL_NAME);
        assertThat(reloaded.get().getPlanType()).isEqualTo(EnrollmentPlanType.HONORS);
    }

    @Test
    @DisplayName("Should persist enrollment record via cascade and remove on drop via orphanRemoval")
    void shouldPersistEnrollmentRecordsViaCascadeAndOrphanRemoval() {
        Course course = new Course(COURSE_NAME, COURSE_CODE, CREDITS);
        courseRepository.saveAndFlush(course);
        major1.addCourse(course);
        majorRepository.saveAndFlush(major1);

        Semester semester = new Semester(SEMESTER_TITLE, START_DATE, END_DATE);
        semesterRepository.saveAndFlush(semester);

        CourseOffering offering = new CourseOffering(course, semester, PROFESSOR, SECTION, true, CAPACITY, CLASS_TIME, null);
        courseOfferingRepository.saveAndFlush(offering);

        Student student = new Student(FULL_NAME, STUDENT_NUMBER, major1, EnrollmentPlanType.REGULAR);
        student.enroll(List.of(offering), new RegularEnrollmentPlanStrategy());
        Student savedStudent = studentRepository.saveAndFlush(student);

        List<EnrollmentRecord> records = enrollmentRecordRepository.findByCourseOfferingId(offering.getId());
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getStudent().getId()).isEqualTo(savedStudent.getId());

        savedStudent.drop(offering);
        studentRepository.flush();

        List<EnrollmentRecord> recordsAfterDrop = enrollmentRecordRepository.findByCourseOfferingId(offering.getId());
        assertThat(recordsAfterDrop).isEmpty();
    }
}
