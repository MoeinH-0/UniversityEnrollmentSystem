package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.service.semesters.archive.ArchiveSemesterCommand;
import com.example.universityenrollmentsystem.service.semesters.archive.ArchiveSemesterCommandHandler;
import com.example.universityenrollmentsystem.service.semesters.create.CreateSemesterCommand;
import com.example.universityenrollmentsystem.service.semesters.create.CreateSemesterCommandHandler;
import com.example.universityenrollmentsystem.service.semesters.details.GetSemesterDetailsQuery;
import com.example.universityenrollmentsystem.service.semesters.details.GetSemesterDetailsQueryHandler;
import com.example.universityenrollmentsystem.service.semesters.details.GetSemesterDetailsResponse;
import com.example.universityenrollmentsystem.service.semesters.offerings.GetSemesterCourseOfferingsQuery;
import com.example.universityenrollmentsystem.service.semesters.offerings.GetSemesterCourseOfferingsQueryHandler;
import com.example.universityenrollmentsystem.service.semesters.offerings.GetSemesterCourseOfferingsResponse;
import com.example.universityenrollmentsystem.service.semesters.reschedule.RescheduleSemesterCommand;
import com.example.universityenrollmentsystem.service.semesters.reschedule.RescheduleSemesterCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/semesters")
public class SemesterController {

    private final CreateSemesterCommandHandler createSemesterCommandHandler;
    private final ArchiveSemesterCommandHandler archiveSemesterCommandHandler;
    private final RescheduleSemesterCommandHandler rescheduleSemesterCommandHandler;
    private final GetSemesterDetailsQueryHandler getSemesterDetailsQueryHandler;
    private final GetSemesterCourseOfferingsQueryHandler getSemesterCourseOfferingsQueryHandler;

    public SemesterController(CreateSemesterCommandHandler createSemesterCommandHandler,
                              ArchiveSemesterCommandHandler archiveSemesterCommandHandler,
                              RescheduleSemesterCommandHandler rescheduleSemesterCommandHandler,
                              GetSemesterDetailsQueryHandler getSemesterDetailsQueryHandler,
                              GetSemesterCourseOfferingsQueryHandler getSemesterCourseOfferingsQueryHandler) {
        this.createSemesterCommandHandler = createSemesterCommandHandler;
        this.archiveSemesterCommandHandler = archiveSemesterCommandHandler;
        this.rescheduleSemesterCommandHandler = rescheduleSemesterCommandHandler;
        this.getSemesterDetailsQueryHandler = getSemesterDetailsQueryHandler;
        this.getSemesterCourseOfferingsQueryHandler = getSemesterCourseOfferingsQueryHandler;
    }

    @PostMapping
    public ResponseEntity<Long> createSemester(@RequestBody CreateSemesterCommand command) {
        Long semesterId = createSemesterCommandHandler.handle(command);
        return ResponseEntity.created(URI.create("/api/v1/semesters/" + semesterId)).body(semesterId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveSemester(@PathVariable Long id) {
        archiveSemesterCommandHandler.handle(new ArchiveSemesterCommand(id));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/schedule")
    public ResponseEntity<Void> rescheduleSemester(@PathVariable Long id,
                                                   @RequestBody RescheduleSemesterRequest request) {
        rescheduleSemesterCommandHandler.handle(new RescheduleSemesterCommand(
                id, request.startDate(), request.endDate()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetSemesterDetailsResponse> getSemesterDetails(@PathVariable Long id) {
        GetSemesterDetailsResponse response = getSemesterDetailsQueryHandler.handle(
                new GetSemesterDetailsQuery(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/offerings")
    public ResponseEntity<GetSemesterCourseOfferingsResponse> getSemesterCourseOfferings(@PathVariable Long id) {
        GetSemesterCourseOfferingsResponse response = getSemesterCourseOfferingsQueryHandler.handle(
                new GetSemesterCourseOfferingsQuery(id));
        return ResponseEntity.ok(response);
    }

    public record RescheduleSemesterRequest(LocalDate startDate, LocalDate endDate) {}
}
