package com.example.universityenrollmentsystem.e2e.enrollment;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.e2e.BaseE2ETest;
import com.example.universityenrollmentsystem.service.semesters.details.GetSemesterDetailsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CapacityAndSectionSwappingE2ETest extends BaseE2ETest {

    private static final String MAJORS_URL = "/api/v1/majors";
    private static final String COURSES_URL = "/api/v1/courses";
    private static final String SEMESTERS_URL = "/api/v1/semesters";
    private static final String OFFERINGS_URL = "/api/v1/course-offerings";
    private static final String CAPACITY_URL = "/api/v1/course-offerings/%d/capacity";
    private static final String ENROLLMENTS_URL = "/api/v1/enrollments";
    private static final String DROP_URL = "/api/v1/enrollments/offerings/%d/students/%d";
    private static final String STUDENTS_URL = "/api/v1/students";

    private static final String MAJOR_NAME = "Computer Science";
    private static final String STUDENT_NAME_A = "Student Alpha";
    private static final String STUDENT_NUMBER_A = "401888001";
    private static final String STUDENT_NAME_B = "Student Beta";
    private static final String STUDENT_NUMBER_B = "401888002";
    private static final String STUDENT_NAME_C = "Student Gamma";
    private static final String STUDENT_NUMBER_C = "401888003";
    private static final String STUDENT_NAME_D = "Student Delta";
    private static final String STUDENT_NUMBER_D = "401888004";

    private static final String PROFESSOR_1 = "Dr. Mohammadi";
    private static final String PROFESSOR_2 = "Dr. Rostami";
    private static final String SECTION_1 = "01";
    private static final String SECTION_2 = "02";
    private static final String CLASS_TIME = "Sat 08:00";
    private static final String EXAM_DATE = "2027-01-12";

    @Test
    @DisplayName("Should prevent concurrent enrollment in different sections of same course but allow swapping via drop")
    void shouldManageSectionSwappingInSameSemester() {
        Long majorId = createMajor(MAJOR_NAME);
        Long courseId = createCourse("Calculus I", "MTH100", 3, List.of(majorId));
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long sec1Offering = createOffering(courseId, semId, PROFESSOR_1, SECTION_1, 30);
        Long sec2Offering = createOffering(courseId, semId, PROFESSOR_2, SECTION_2, 30);

        Long studentId = registerStudent(STUDENT_NAME_A, STUDENT_NUMBER_A, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(sec1Offering), HttpStatus.OK);

        enroll(studentId, List.of(sec2Offering), HttpStatus.CONFLICT);

        dropOffering(sec1Offering, studentId, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(sec2Offering), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should manage single capacity seat contention and release seat on drop")
    void shouldManageCapacityContentionAndReleaseOnDrop() {
        Long majorId = createMajor(MAJOR_NAME);
        Long courseId = createCourse("Discrete Math", "CS210", 3, List.of(majorId));
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long singleSeatOffering = createOffering(courseId, semId, PROFESSOR_1, SECTION_1, 1);

        Long studentA = registerStudent(STUDENT_NAME_A, STUDENT_NUMBER_A, majorId, EnrollmentPlanType.REGULAR);
        Long studentB = registerStudent(STUDENT_NAME_B, STUDENT_NUMBER_B, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentA, List.of(singleSeatOffering), HttpStatus.OK);

        enroll(studentB, List.of(singleSeatOffering), HttpStatus.CONFLICT);

        dropOffering(singleSeatOffering, studentA, HttpStatus.NO_CONTENT);

        enroll(studentB, List.of(singleSeatOffering), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should block capacity reduction below enrolled count and allow enrollment after capacity increase")
    void shouldBlockCapacityReductionBelowEnrolledCount() {
        Long majorId = createMajor(MAJOR_NAME);
        Long courseId = createCourse("Linear Algebra", "MTH205", 3, List.of(majorId));
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long offeringId = createOffering(courseId, semId, PROFESSOR_1, SECTION_1, 3);

        Long studentA = registerStudent(STUDENT_NAME_A, STUDENT_NUMBER_A, majorId, EnrollmentPlanType.REGULAR);
        Long studentB = registerStudent(STUDENT_NAME_B, STUDENT_NUMBER_B, majorId, EnrollmentPlanType.REGULAR);
        Long studentC = registerStudent(STUDENT_NAME_C, STUDENT_NUMBER_C, majorId, EnrollmentPlanType.REGULAR);
        Long studentD = registerStudent(STUDENT_NAME_D, STUDENT_NUMBER_D, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentA, List.of(offeringId), HttpStatus.OK);
        enroll(studentB, List.of(offeringId), HttpStatus.OK);
        enroll(studentC, List.of(offeringId), HttpStatus.OK);

        updateCapacity(offeringId, 2, HttpStatus.BAD_REQUEST);

        updateCapacity(offeringId, 5, HttpStatus.NO_CONTENT);

        enroll(studentD, List.of(offeringId), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should allow re-enrolling in same offering in same semester after drop")
    void shouldAllowDropAndReEnrollInSameOffering() {
        Long majorId = createMajor(MAJOR_NAME);
        Long courseId = createCourse("Physics I", "PHY101", 3, List.of(majorId));
        Long semId = createSemester("Fall 2026", "2026-09-20", "2027-01-20");

        Long offeringId = createOffering(courseId, semId, PROFESSOR_1, SECTION_1, 20);

        Long studentId = registerStudent(STUDENT_NAME_A, STUDENT_NUMBER_A, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offeringId), HttpStatus.OK);

        dropOffering(offeringId, studentId, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(offeringId), HttpStatus.OK);

        enroll(studentId, List.of(offeringId), HttpStatus.CONFLICT);
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

    private Long createOffering(Long courseId, Long semesterId, String professor, String section, int capacity) {
        GetSemesterDetailsResponse sem = restTemplate.getForEntity(
                "/api/v1/semesters/" + semesterId,
                GetSemesterDetailsResponse.class
        ).getBody();
        String examDate = (sem != null) ? sem.endDate().minusDays(5).toString() : EXAM_DATE;

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
                """.formatted(courseId, semesterId, professor, section, capacity, CLASS_TIME, examDate);
        ResponseEntity<Long> response = restTemplate.postForEntity(OFFERINGS_URL, createJsonEntity(json), Long.class);
        return response.getBody();
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

    private void updateCapacity(Long offeringId, int capacity, HttpStatus expectedStatus) {
        String json = "{\"capacity\": %d}".formatted(capacity);
        ResponseEntity<Void> response = restTemplate.exchange(
                String.format(CAPACITY_URL, offeringId),
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

    private void dropOffering(Long offeringId, Long studentId, HttpStatus expectedStatus) {
        ResponseEntity<Void> response = restTemplate.exchange(
                String.format(DROP_URL, offeringId, studentId),
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus.value());
    }
}
