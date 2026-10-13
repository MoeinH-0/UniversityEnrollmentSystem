package com.example.universityenrollmentsystem.e2e.academic;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.e2e.BaseE2ETest;
import com.example.universityenrollmentsystem.service.courseOfferings.students.GetCourseOfferingStudentsResponse;
import com.example.universityenrollmentsystem.service.semesters.offerings.GetSemesterCourseOfferingsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SemesterAndSchedulingE2ETest extends BaseE2ETest {

    private static final String MAJORS_URL = "/api/v1/majors";
    private static final String COURSES_URL = "/api/v1/courses";
    private static final String SEMESTERS_URL = "/api/v1/semesters";
    private static final String SEMESTER_SCHEDULE_URL = "/api/v1/semesters/%d/schedule";
    private static final String SEMESTER_OFFERINGS_URL = "/api/v1/semesters/%d/offerings";
    private static final String OFFERINGS_URL = "/api/v1/course-offerings";
    private static final String OFFERING_CANCEL_URL = "/api/v1/course-offerings/%d";
    private static final String OFFERING_PROF_URL = "/api/v1/course-offerings/%d/professor";
    private static final String OFFERING_EXAM_URL = "/api/v1/course-offerings/%d/exam-date";
    private static final String OFFERING_STUDENTS_URL = "/api/v1/course-offerings/%d/students";
    private static final String ENROLLMENTS_URL = "/api/v1/enrollments";
    private static final String STUDENTS_URL = "/api/v1/students";

    private static final String MAJOR_NAME = "Computer Science";
    private static final String STUDENT_NAME = "Babak Khorrami";
    private static final String STUDENT_NUMBER = "401999888";

    private static final String PROFESSOR_A = "Dr. Sharifi";
    private static final String PROFESSOR_B = "Dr. Kazemi";
    private static final String SECTION = "01";
    private static final int CAPACITY = 30;
    private static final String CLASS_TIME = "Sat 10:00";

    @Test
    @DisplayName("Should cancel course offering preventing new enrollments and hiding from semester catalog")
    void shouldCancelCourseOfferingAndBlockEnrollments() {
        Long majorId = createMajor(MAJOR_NAME);
        Long courseId = createCourse("OS Concepts", "CS330", 3, List.of(majorId));
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long offeringId = createOffering(courseId, semId, PROFESSOR_A, "2027-01-10", HttpStatus.CREATED);

        ResponseEntity<Void> cancelResponse = restTemplate.exchange(
                String.format(OFFERING_CANCEL_URL, offeringId),
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<GetSemesterCourseOfferingsResponse> semOfferings = restTemplate.getForEntity(
                String.format(SEMESTER_OFFERINGS_URL, semId),
                GetSemesterCourseOfferingsResponse.class
        );
        assertThat(semOfferings.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(semOfferings.getBody()).isNotNull();
        assertThat(semOfferings.getBody().offerings()).noneMatch(o -> o.offeringId().equals(offeringId));

        Long studentId = registerStudent(STUDENT_NAME, STUDENT_NUMBER, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offeringId), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should validate exam dates against exact semester start and end boundaries")
    void shouldValidateExamDateBoundariesAgainstSemesterDates() {
        Long majorId = createMajor(MAJOR_NAME);
        Long courseId = createCourse("Databases", "CS340", 3, List.of(majorId));
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        createOffering(courseId, semId, PROFESSOR_A, "2026-09-19", HttpStatus.BAD_REQUEST);

        Long offeringId = createOffering(courseId, semId, PROFESSOR_A, "2026-09-20", HttpStatus.CREATED);

        updateExamDate(offeringId, "2027-01-20", HttpStatus.NO_CONTENT);

        updateExamDate(offeringId, "2027-01-21", HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should allow in-flight offering modifications of professor and exam date without disrupting enrollments")
    void shouldAllowInFlightOfferingModificationsWithoutDisruptingEnrollments() {
        Long majorId = createMajor(MAJOR_NAME);
        Long courseId = createCourse("Algorithms", "CS350", 3, List.of(majorId));
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long offeringId = createOffering(courseId, semId, PROFESSOR_A, "2027-01-10", HttpStatus.CREATED);

        Long studentId = registerStudent(STUDENT_NAME, STUDENT_NUMBER, majorId, EnrollmentPlanType.REGULAR);
        enroll(studentId, List.of(offeringId), HttpStatus.OK);

        String updateProfJson = "{\"professorName\": \"%s\"}".formatted(PROFESSOR_B);
        ResponseEntity<Void> profResponse = restTemplate.exchange(
                String.format(OFFERING_PROF_URL, offeringId),
                HttpMethod.PUT,
                createJsonEntity(updateProfJson),
                Void.class
        );
        assertThat(profResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        updateExamDate(offeringId, "2027-01-07", HttpStatus.NO_CONTENT);

        ResponseEntity<GetCourseOfferingStudentsResponse> studentsResponse = restTemplate.getForEntity(
                String.format(OFFERING_STUDENTS_URL, offeringId),
                GetCourseOfferingStudentsResponse.class
        );
        assertThat(studentsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(studentsResponse.getBody()).isNotNull();
        assertThat(studentsResponse.getBody().students()).hasSize(1);
        assertThat(studentsResponse.getBody().students().get(0).studentId()).isEqualTo(studentId);

        ResponseEntity<GetSemesterCourseOfferingsResponse> offeringsResponse = restTemplate.getForEntity(
                String.format(SEMESTER_OFFERINGS_URL, semId),
                GetSemesterCourseOfferingsResponse.class
        );
        assertThat(offeringsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(offeringsResponse.getBody()).isNotNull();
        assertThat(offeringsResponse.getBody().offerings().get(0).professorName()).isEqualTo(PROFESSOR_B);
        assertThat(offeringsResponse.getBody().offerings().get(0).examDate().toString()).isEqualTo("2027-01-07");
    }

    @Test
    @DisplayName("Should enforce new semester bounds after reschedule when modifying offering exam dates")
    void shouldEnforceNewSemesterBoundsAfterReschedule() {
        Long majorId = createMajor(MAJOR_NAME);
        Long courseId = createCourse("Network Security", "CS360", 3, List.of(majorId));
        Long semId = createSemester("Fall 2026", "2026-10-01", "2027-02-15");

        Long offeringId = createOffering(courseId, semId, PROFESSOR_A, "2027-02-10", HttpStatus.CREATED);

        String rescheduleJson = """
                {
                    "startDate": "2026-10-01",
                    "endDate": "2027-01-30"
                }
                """;
        ResponseEntity<Void> rescheduleResponse = restTemplate.exchange(
                String.format(SEMESTER_SCHEDULE_URL, semId),
                HttpMethod.PUT,
                createJsonEntity(rescheduleJson),
                Void.class
        );
        assertThat(rescheduleResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        updateExamDate(offeringId, "2027-02-05", HttpStatus.BAD_REQUEST);

        updateExamDate(offeringId, "2027-01-25", HttpStatus.NO_CONTENT);
    }

    private Long createMajor(String name) {
        String json = "{\"name\": \"%s\"}".formatted(name);
        ResponseEntity<Long> response = restTemplate.postForEntity(MAJORS_URL, createJsonEntity(json), Long.class);
        return response.getBody();
    }

    private Long createCourse(String name, String courseCode, int credits, List<Long> majorIds) {
        String json = """
                {
                    "name": "%s",
                    "courseCode": "%s",
                    "credits": %d,
                    "majorIds": %s,
                    "prerequisiteIds": []
                }
                """.formatted(name, courseCode, credits, majorIds.toString());
        ResponseEntity<Long> response = restTemplate.postForEntity(COURSES_URL, createJsonEntity(json), Long.class);
        return response.getBody();
    }

    private Long createSemester(String title, String startDate, String endDate) {
        String json = """
                {
                    "title": "%s",
                    "startDate": "%s",
                    "endDate": "%s"
                }
                """.formatted(title, startDate, endDate);
        ResponseEntity<Long> response = restTemplate.postForEntity(SEMESTERS_URL, createJsonEntity(json), Long.class);
        return response.getBody();
    }

    private Long createOffering(Long courseId, Long semesterId, String professor, String examDate, HttpStatus expectedStatus) {
        String json = """
                {
                    "courseId": %d,
                    "semesterId": %d,
                    "professorName": "%s",
                    "section": "%s",
                    "guestAllowed": true,
                    "capacity": %d,
                    "classTime": "%s",
                    "examDate": "%s"
                }
                """.formatted(courseId, semesterId, professor, SECTION, CAPACITY, CLASS_TIME, examDate);
        ResponseEntity<String> response = restTemplate.postForEntity(OFFERINGS_URL, createJsonEntity(json), String.class);
        assertThat(response.getStatusCode().value()).as(response.getBody()).isEqualTo(expectedStatus.value());
        if (response.getStatusCode().value() == HttpStatus.CREATED.value())
            return Long.valueOf(response.getBody());

        return null;
    }

    private Long registerStudent(String fullName, String studentNumber, Long majorId, EnrollmentPlanType planType) {
        String json = """
                {
                    "fullName": "%s",
                    "studentNumber": "%s",
                    "majorId": %d,
                    "planType": "%s"
                }
                """.formatted(fullName, studentNumber, majorId, planType.name());
        ResponseEntity<Long> response = restTemplate.postForEntity(STUDENTS_URL, createJsonEntity(json), Long.class);
        return response.getBody();
    }

    private void updateExamDate(Long offeringId, String examDate, HttpStatus expectedStatus) {
        String json = "{\"examDate\": \"%s\"}".formatted(examDate);
        ResponseEntity<Void> response = restTemplate.exchange(
                String.format(OFFERING_EXAM_URL, offeringId),
                HttpMethod.PUT,
                createJsonEntity(json),
                Void.class
        );
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus.value());
    }

    private void enroll(Long studentId, List<Long> offeringIds, HttpStatus expectedStatus) {
        ResponseEntity<String> response = null;
        for (Long offeringId : offeringIds) {
            String json = """
                    {
                        "studentId": %d,
                        "courseOfferingId": %d
                    }
                    """.formatted(studentId, offeringId);
            response = restTemplate.postForEntity(ENROLLMENTS_URL, createJsonEntity(json), String.class);
            if (response.getStatusCode().value() != HttpStatus.OK.value())
                break;
        }
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).as(response.getBody()).isEqualTo(expectedStatus.value());
    }
}
