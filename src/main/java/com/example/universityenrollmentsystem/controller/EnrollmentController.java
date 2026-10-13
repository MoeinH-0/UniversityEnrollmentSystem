package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.service.enrollments.drop.DropCourseOfferingCommand;
import com.example.universityenrollmentsystem.service.enrollments.drop.DropCourseOfferingCommandHandler;
import com.example.universityenrollmentsystem.service.enrollments.eligible.GetEligibleCoursesQuery;
import com.example.universityenrollmentsystem.service.enrollments.eligible.GetEligibleCoursesQueryHandler;
import com.example.universityenrollmentsystem.service.enrollments.eligible.GetEligibleCoursesResponse;
import com.example.universityenrollmentsystem.service.enrollments.submit.SubmitEnrollmentCommand;
import com.example.universityenrollmentsystem.service.enrollments.submit.SubmitEnrollmentCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final SubmitEnrollmentCommandHandler submitEnrollmentCommandHandler;
    private final DropCourseOfferingCommandHandler dropCourseOfferingCommandHandler;
    private final GetEligibleCoursesQueryHandler getEligibleCoursesQueryHandler;

    public EnrollmentController(SubmitEnrollmentCommandHandler submitEnrollmentCommandHandler,
                                DropCourseOfferingCommandHandler dropCourseOfferingCommandHandler,
                                GetEligibleCoursesQueryHandler getEligibleCoursesQueryHandler) {
        this.submitEnrollmentCommandHandler = submitEnrollmentCommandHandler;
        this.dropCourseOfferingCommandHandler = dropCourseOfferingCommandHandler;
        this.getEligibleCoursesQueryHandler = getEligibleCoursesQueryHandler;
    }

    @PostMapping
    public ResponseEntity<Void> submitEnrollment(@RequestBody SubmitEnrollmentCommand command) {
        submitEnrollmentCommandHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/offerings/{courseOfferingId}/students/{studentId}")
    public ResponseEntity<Void> dropCourseOffering(@PathVariable Long courseOfferingId,
                                                   @PathVariable Long studentId) {
        dropCourseOfferingCommandHandler.handle(new DropCourseOfferingCommand(courseOfferingId, studentId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/eligible")
    public ResponseEntity<GetEligibleCoursesResponse> getEligibleCourses(@RequestParam Long studentId,
                                                                         @RequestParam Long semesterId) {
        GetEligibleCoursesResponse response = getEligibleCoursesQueryHandler.handle(
                new GetEligibleCoursesQuery(studentId, semesterId));
        return ResponseEntity.ok(response);
    }
}
