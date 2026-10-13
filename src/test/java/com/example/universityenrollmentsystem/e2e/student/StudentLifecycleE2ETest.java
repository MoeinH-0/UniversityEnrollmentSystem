package com.example.universityenrollmentsystem.e2e.student;

import com.example.universityenrollmentsystem.domain.entity.student.EnrollmentPlanType;
import com.example.universityenrollmentsystem.domain.entity.student.StudentStatus;
import com.example.universityenrollmentsystem.e2e.BaseE2ETest;
import com.example.universityenrollmentsystem.service.courses.statistics.GetCourseStatisticsResponse;
import com.example.universityenrollmentsystem.service.students.details.GetStudentDetailsResponse;
import com.example.universityenrollmentsystem.service.students.transcript.GetStudentTranscriptResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentLifecycleE2ETest extends BaseE2ETest {

    private static final String MAJORS_URL = "/api/v1/majors";
    private static final String COURSES_URL = "/api/v1/courses";
    private static final String SEMESTERS_URL = "/api/v1/semesters";
    private static final String OFFERINGS_URL = "/api/v1/course-offerings";
    private static final String ENROLLMENTS_URL = "/api/v1/enrollments";
    private static final String STUDENTS_URL = "/api/v1/students";
    private static final String STUDENT_DETAILS_URL = "/api/v1/students/%d";
    private static final String STUDENT_STATUS_URL = "/api/v1/students/%d/status";
    private static final String STUDENT_CONTACT_URL = "/api/v1/students/%d/contact";
    private static final String STUDENT_MAJOR_URL = "/api/v1/students/%d/major";
    private static final String STUDENT_TRANSCRIPT_URL = "/api/v1/students/%d/transcript";
    private static final String COURSE_STATS_URL = "/api/v1/courses/%d/statistics";
    private static final String DROP_URL = "/api/v1/enrollments/offerings/%d/students/%d";

    private static final String MAJOR_NAME_1 = "Computer Science";
    private static final String MAJOR_NAME_2 = "Software Engineering";
    private static final String MAJOR_HARDWARE = "Computer Hardware";
    private static final String STUDENT_NAME = "Kasra Alavi";
    private static final String STUDENT_NUMBER = "401123456";
    private static final String NEW_EMAIL = "kasra.alavi@uni.edu";
    private static final String NEW_PHONE = "09129876543";
    private static final Long NON_EXISTING_MAJOR_ID = 99999L;

    private static final String SEMESTER_TITLE = "Fall 2026";
    private static final String START_DATE = "2026-09-20";
    private static final String END_DATE = "2027-01-20";
    private static final String PROFESSOR = "Dr. Tehrani";
    private static final String SECTION = "01";
    private static final int CAPACITY = 30;
    private static final String CLASS_TIME = "Sat 10:00";
    private static final String EXAM_DATE = "2027-01-10";

    @Test
    @DisplayName("Should execute complete student lifecycle from registration to status and major changes")
    void shouldExecuteCompleteStudentLifecycleSuccessfully() {
        String createMajorJson1 = """
                {
                    "name": "%s"
                }
                """.formatted(MAJOR_NAME_1);

        ResponseEntity<Long> majorResponse1 = restTemplate.postForEntity(
                MAJORS_URL,
                createJsonEntity(createMajorJson1),
                Long.class
        );

        assertThat(majorResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long majorId1 = majorResponse1.getBody();
        assertThat(majorId1).isNotNull();

        String registerStudentJson = String.format("""
                {
                    "fullName": "%s",
                    "studentNumber": "%s",
                    "majorId": %d,
                    "planType": "%s"
                }
                """, STUDENT_NAME, STUDENT_NUMBER, majorId1, EnrollmentPlanType.REGULAR.name());

        ResponseEntity<Long> studentResponse = restTemplate.postForEntity(
                STUDENTS_URL,
                createJsonEntity(registerStudentJson),
                Long.class
        );

        assertThat(studentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long studentId = studentResponse.getBody();
        assertThat(studentId).isNotNull();

        ResponseEntity<GetStudentDetailsResponse> detailsResponse1 = restTemplate.getForEntity(
                String.format(STUDENT_DETAILS_URL, studentId),
                GetStudentDetailsResponse.class
        );

        assertThat(detailsResponse1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detailsResponse1.getBody()).isNotNull();
        assertThat(detailsResponse1.getBody().fullName()).isEqualTo(STUDENT_NAME);
        assertThat(detailsResponse1.getBody().studentNumber()).isEqualTo(STUDENT_NUMBER);
        assertThat(detailsResponse1.getBody().majorName()).isEqualTo(MAJOR_NAME_1);
        assertThat(detailsResponse1.getBody().status()).isEqualTo(StudentStatus.ACTIVE);

        String updateContactJson = String.format("""
                {
                    "email": "%s",
                    "phoneNumber": "%s"
                }
                """, NEW_EMAIL, NEW_PHONE);

        ResponseEntity<Void> contactResponse = restTemplate.exchange(
                String.format(STUDENT_CONTACT_URL, studentId),
                HttpMethod.PUT,
                createJsonEntity(updateContactJson),
                Void.class
        );

        assertThat(contactResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        String changeStatusJson = String.format("""
                {
                    "status": "%s"
                }
                """, StudentStatus.WITHDRAWN.name());

        ResponseEntity<Void> statusResponse = restTemplate.exchange(
                String.format(STUDENT_STATUS_URL, studentId),
                HttpMethod.PUT,
                createJsonEntity(changeStatusJson),
                Void.class
        );

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        String changeMajorInvalidJson = String.format("""
                {
                    "majorId": %d
                }
                """, NON_EXISTING_MAJOR_ID);

        ResponseEntity<String> invalidMajorResponse = restTemplate.exchange(
                String.format(STUDENT_MAJOR_URL, studentId),
                HttpMethod.PUT,
                createJsonEntity(changeMajorInvalidJson),
                String.class
        );

        assertThat(invalidMajorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        String createMajorJson2 = """
                {
                    "name": "%s"
                }
                """.formatted(MAJOR_NAME_2);

        ResponseEntity<Long> majorResponse2 = restTemplate.postForEntity(
                MAJORS_URL,
                createJsonEntity(createMajorJson2),
                Long.class
        );

        assertThat(majorResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long majorId2 = majorResponse2.getBody();
        assertThat(majorId2).isNotNull();

        String changeMajorValidJson = String.format("""
                {
                    "majorId": %d
                }
                """, majorId2);

        ResponseEntity<Void> changeMajorResponse = restTemplate.exchange(
                String.format(STUDENT_MAJOR_URL, studentId),
                HttpMethod.PUT,
                createJsonEntity(changeMajorValidJson),
                Void.class
        );

        assertThat(changeMajorResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<GetStudentDetailsResponse> finalDetailsResponse = restTemplate.getForEntity(
                String.format(STUDENT_DETAILS_URL, studentId),
                GetStudentDetailsResponse.class
        );

        assertThat(finalDetailsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(finalDetailsResponse.getBody()).isNotNull();
        assertThat(finalDetailsResponse.getBody().email()).isEqualTo(NEW_EMAIL);
        assertThat(finalDetailsResponse.getBody().phoneNumber()).isEqualTo(NEW_PHONE);
        assertThat(finalDetailsResponse.getBody().status()).isEqualTo(StudentStatus.WITHDRAWN);
        assertThat(finalDetailsResponse.getBody().majorName()).isEqualTo(MAJOR_NAME_2);
    }

    @Test
    @DisplayName("Should enforce student suspension rules blocking new enrollments while allowing drops")
    void shouldEnforceSuspensionRulesOnEnrollmentAndDrop() {
        Long majorId = createMajor(MAJOR_NAME_1);
        Long course1Id = createCourse("Programming", "CS101", 3, List.of(majorId));
        Long course2Id = createCourse("Algorithms", "CS102", 3, List.of(majorId));
        Long course3Id = createCourse("Networks", "CS103", 3, List.of(majorId));

        Long semesterId = createSemester(SEMESTER_TITLE, START_DATE, END_DATE);
        Long offering1Id = createOffering(course1Id, semesterId, CAPACITY);
        Long offering2Id = createOffering(course2Id, semesterId, CAPACITY);
        Long offering3Id = createOffering(course3Id, semesterId, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME, STUDENT_NUMBER, majorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offering1Id, offering2Id), HttpStatus.OK);

        updateStudentStatus(studentId, StudentStatus.WITHDRAWN, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(offering3Id), HttpStatus.BAD_REQUEST);

        dropOffering(offering1Id, studentId, HttpStatus.NO_CONTENT);

        updateStudentStatus(studentId, StudentStatus.ACTIVE, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(offering3Id), HttpStatus.OK);
    }

    @Test
    @DisplayName("Should handle change of major mid-semester while preserving historical records and updating statistics")
    void shouldHandleChangeOfMajorMidSemesterCorrectly() {
        Long softwareMajorId = createMajor(MAJOR_NAME_2);
        Long hardwareMajorId = createMajor(MAJOR_HARDWARE);

        Long softwareCourse1Id = createCourse("Software Design", "SE201", 3, List.of(softwareMajorId));
        Long softwareCourse2Id = createCourse("Software Testing", "SE202", 3, List.of(softwareMajorId));

        Long semesterId = createSemester(SEMESTER_TITLE, START_DATE, END_DATE);
        Long offering1Id = createOffering(softwareCourse1Id, semesterId, CAPACITY);
        Long offering2Id = createOffering(softwareCourse2Id, semesterId, CAPACITY);

        Long studentId = registerStudent(STUDENT_NAME, STUDENT_NUMBER, softwareMajorId, EnrollmentPlanType.REGULAR);

        enroll(studentId, List.of(offering1Id), HttpStatus.OK);

        changeStudentMajor(studentId, hardwareMajorId, HttpStatus.NO_CONTENT);

        enroll(studentId, List.of(offering2Id), HttpStatus.UNPROCESSABLE_CONTENT);

        ResponseEntity<GetStudentTranscriptResponse> transcriptResponse = restTemplate.getForEntity(
                String.format(STUDENT_TRANSCRIPT_URL, studentId),
                GetStudentTranscriptResponse.class
        );

        assertThat(transcriptResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transcriptResponse.getBody()).isNotNull();
        assertThat(transcriptResponse.getBody().majorName()).isEqualTo(MAJOR_HARDWARE);
        assertThat(transcriptResponse.getBody().semesters()).hasSize(1);
        assertThat(transcriptResponse.getBody().semesters().get(0).courses().get(0).courseCode()).isEqualTo("SE201");

        ResponseEntity<GetCourseStatisticsResponse> statsResponse = restTemplate.getForEntity(
                String.format(COURSE_STATS_URL, softwareCourse1Id),
                GetCourseStatisticsResponse.class
        );

        assertThat(statsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statsResponse.getBody()).isNotNull();
        assertThat(statsResponse.getBody().byMajor()).anyMatch(m -> m.majorName().equals(MAJOR_HARDWARE));
    }

    private Long createMajor(String name) {
        String json = "{\"name\": \"%s\"}".formatted(name);
        ResponseEntity<Long> response = restTemplate.postForEntity(MAJORS_URL, createJsonEntity(json), Long.class);
        return response.getBody();
    }

    private Long createCourse(String name, String courseCode, int credits, List<Long> majorIds) {
        String majorsArray = majorIds.toString();
        String json = """
                {
                    "name": "%s",
                    "courseCode": "%s",
                    "credits": %d,
                    "majorIds": %s,
                    "prerequisiteIds": []
                }
                """.formatted(name, courseCode, credits, majorsArray);
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

    private Long createOffering(Long courseId, Long semesterId, int capacity) {
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
                """.formatted(courseId, semesterId, PROFESSOR, SECTION, capacity, CLASS_TIME, EXAM_DATE);
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
            if (response.getStatusCode() != HttpStatus.OK)
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
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }

    private void updateStudentStatus(Long studentId, StudentStatus status, HttpStatus expectedStatus) {
        String json = "{\"status\": \"%s\"}".formatted(status.name());
        ResponseEntity<Void> response = restTemplate.exchange(
                String.format(STUDENT_STATUS_URL, studentId),
                HttpMethod.PUT,
                createJsonEntity(json),
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }

    private void changeStudentMajor(Long studentId, Long majorId, HttpStatus expectedStatus) {
        String json = "{\"majorId\": %d}".formatted(majorId);
        ResponseEntity<Void> response = restTemplate.exchange(
                String.format(STUDENT_MAJOR_URL, studentId),
                HttpMethod.PUT,
                createJsonEntity(json),
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }
}
