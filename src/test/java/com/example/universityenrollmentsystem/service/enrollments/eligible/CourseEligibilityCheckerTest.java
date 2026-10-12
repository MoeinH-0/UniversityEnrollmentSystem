package com.example.universityenrollmentsystem.service.enrollments.eligible;

import com.example.universityenrollmentsystem.domain.entity.academic.Major;
import com.example.universityenrollmentsystem.domain.entity.academic.Semester;
import com.example.universityenrollmentsystem.domain.entity.course.Course;
import com.example.universityenrollmentsystem.domain.entity.course.CourseOffering;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentRecord;
import com.example.universityenrollmentsystem.domain.entity.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourseEligibilityCheckerTest {

    private static final int CREDITS = 3;
    private static final int CAPACITY = 30;
    private static final Double PASSING_GRADE = 15.0;
    private static final Double FAILING_GRADE = 8.0;

    private CourseEligibilityChecker checker;
    private Major ceMajor;
    private Major eeMajor;
    private Semester semester;
    private Course course;
    private CourseOffering offering;

    @BeforeEach
    void setUp() {
        checker = new CourseEligibilityChecker();
        ceMajor = new Major("Computer Engineering");
        eeMajor = new Major("Electrical Engineering");
        semester = new Semester("Fall 2026", LocalDate.of(2026, 9, 23), LocalDate.of(2027, 1, 20));

        course = new Course("Algorithms", "CS302", CREDITS);
        ReflectionTestUtils.setField(course, "id", 10L);
        ceMajor.addCourse(course);

        offering = new CourseOffering(course, semester, "Dr. Ahmadi", "01", true, CAPACITY, "Mon 10:00", null);
        ReflectionTestUtils.setField(offering, "id", 100L);
    }

    @SuppressWarnings("unchecked")
    private void attachEnrollment(Student student, EnrollmentRecord record) {
        List<EnrollmentRecord> list = (List<EnrollmentRecord>) ReflectionTestUtils.getField(student, "enrollments");
        if (list != null)
            list.add(record);
    }

    @Test
    @DisplayName("Scenario 298: Should return false when course was already passed")
    void shouldReturnFalseWhenCourseAlreadyPassed() {
        Student student = new Student("Ali", "401111", ceMajor, EnrollmentPlanType.REGULAR);
        EnrollmentRecord record = new EnrollmentRecord(student, offering);
        record.assignGrade(PASSING_GRADE);
        attachEnrollment(student, record);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isFalse();
    }

    @Test
    @DisplayName("Scenario 299: Should return false when course is currently in progress")
    void shouldReturnFalseWhenCourseInProgress() {
        Student student = new Student("Ali", "401111", ceMajor, EnrollmentPlanType.REGULAR);
        EnrollmentRecord record = new EnrollmentRecord(student, offering);
        attachEnrollment(student, record);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isFalse();
    }

    @Test
    @DisplayName("Scenario 300: Should return false when prerequisite has not been taken")
    void shouldReturnFalseWhenPrerequisiteNotTaken() {
        Course prerequisite = new Course("Data Structures", "CS201", CREDITS);
        ReflectionTestUtils.setField(prerequisite, "id", 5L);
        course.addPrerequisite(prerequisite);

        Student student = new Student("Ali", "401111", ceMajor, EnrollmentPlanType.REGULAR);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isFalse();
    }

    @Test
    @DisplayName("Scenario 301: Should return false when prerequisite was failed")
    void shouldReturnFalseWhenPrerequisiteFailed() {
        Course prerequisite = new Course("Data Structures", "CS201", CREDITS);
        ReflectionTestUtils.setField(prerequisite, "id", 5L);
        course.addPrerequisite(prerequisite);

        CourseOffering prereqOffering = new CourseOffering(prerequisite, semester, "Dr. Rezaei", "01", true, CAPACITY, "Tue 10:00", null);
        ReflectionTestUtils.setField(prereqOffering, "id", 50L);

        Student student = new Student("Ali", "401111", ceMajor, EnrollmentPlanType.REGULAR);
        EnrollmentRecord record = new EnrollmentRecord(student, prereqOffering);
        record.assignGrade(FAILING_GRADE);
        attachEnrollment(student, record);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isFalse();
    }

    @Test
    @DisplayName("Scenario 302: Should return false when prerequisite is in progress")
    void shouldReturnFalseWhenPrerequisiteInProgress() {
        Course prerequisite = new Course("Data Structures", "CS201", CREDITS);
        ReflectionTestUtils.setField(prerequisite, "id", 5L);
        course.addPrerequisite(prerequisite);

        CourseOffering prereqOffering = new CourseOffering(prerequisite, semester, "Dr. Rezaei", "01", true, CAPACITY, "Tue 10:00", null);
        ReflectionTestUtils.setField(prereqOffering, "id", 50L);

        Student student = new Student("Ali", "401111", ceMajor, EnrollmentPlanType.REGULAR);
        EnrollmentRecord record = new EnrollmentRecord(student, prereqOffering);
        attachEnrollment(student, record);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isFalse();
    }

    @Test
    @DisplayName("Scenario 303: Should return true for GUEST student when guestAllowed is true")
    void shouldReturnTrueForGuestStudentWhenGuestAllowed() {
        Student student = new Student("Guest Student", "999111", ceMajor, EnrollmentPlanType.GUEST);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isTrue();
    }

    @Test
    @DisplayName("Scenario 304: Should return false for GUEST student when guestAllowed is false")
    void shouldReturnFalseForGuestStudentWhenGuestNotAllowed() {
        offering.updateGuestPolicy(false);
        Student student = new Student("Guest Student", "999111", ceMajor, EnrollmentPlanType.GUEST);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isFalse();
    }

    @Test
    @DisplayName("Scenario 305: Should return true for HONORS student even outside major")
    void shouldReturnTrueForHonorsStudentOutsideMajor() {
        Student student = new Student("Honors Student", "401222", eeMajor, EnrollmentPlanType.HONORS);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isTrue();
    }

    @Test
    @DisplayName("Scenario 306: Should return true for REGULAR student from own major")
    void shouldReturnTrueForRegularStudentFromOwnMajor() {
        Student student = new Student("Regular Student", "401333", ceMajor, EnrollmentPlanType.REGULAR);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isTrue();
    }

    @Test
    @DisplayName("Scenario 307: Should return false for REGULAR student outside major")
    void shouldReturnFalseForRegularStudentOutsideMajor() {
        Student student = new Student("Regular Student", "401333", eeMajor, EnrollmentPlanType.REGULAR);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isFalse();
    }

    @Test
    @DisplayName("Scenario 308: Should return true for PROBATION student from own major")
    void shouldReturnTrueForProbationStudentFromOwnMajor() {
        Student student = new Student("Probation Student", "401444", ceMajor, EnrollmentPlanType.PROBATION);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isTrue();
    }

    @Test
    @DisplayName("Scenario 309: Should return false for PROBATION student outside major")
    void shouldReturnFalseForProbationStudentOutsideMajor() {
        Student student = new Student("Probation Student", "401444", eeMajor, EnrollmentPlanType.PROBATION);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isFalse();
    }

    @Test
    @DisplayName("Scenario 310: Should return true when course was previously failed (can be retaken)")
    void shouldReturnTrueWhenCoursePreviouslyFailed() {
        Student student = new Student("Retake Student", "401555", ceMajor, EnrollmentPlanType.REGULAR);
        EnrollmentRecord record = new EnrollmentRecord(student, offering);
        record.assignGrade(FAILING_GRADE);
        attachEnrollment(student, record);

        boolean eligible = checker.isEligible(student, offering);

        assertThat(eligible).isTrue();
    }
}
