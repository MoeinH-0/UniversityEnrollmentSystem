package com.example.universityenrollmentsystem.controller;

import com.example.universityenrollmentsystem.service.courses.archive.ArchiveCourseCommand;
import com.example.universityenrollmentsystem.service.courses.archive.ArchiveCourseCommandHandler;
import com.example.universityenrollmentsystem.service.courses.create.CreateCourseCommand;
import com.example.universityenrollmentsystem.service.courses.create.CreateCourseCommandHandler;
import com.example.universityenrollmentsystem.service.courses.credits.UpdateCourseCreditsCommand;
import com.example.universityenrollmentsystem.service.courses.credits.UpdateCourseCreditsCommandHandler;
import com.example.universityenrollmentsystem.service.courses.details.GetCourseDetailsQuery;
import com.example.universityenrollmentsystem.service.courses.details.GetCourseDetailsQueryHandler;
import com.example.universityenrollmentsystem.service.courses.details.GetCourseDetailsResponse;
import com.example.universityenrollmentsystem.service.courses.prerequisites.UpdateCoursePrerequisitesCommand;
import com.example.universityenrollmentsystem.service.courses.prerequisites.UpdateCoursePrerequisitesCommandHandler;
import com.example.universityenrollmentsystem.service.courses.statistics.GetCourseStatisticsQuery;
import com.example.universityenrollmentsystem.service.courses.statistics.GetCourseStatisticsQueryHandler;
import com.example.universityenrollmentsystem.service.courses.statistics.GetCourseStatisticsResponse;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CreateCourseCommandHandler createCourseCommandHandler;
    private final ArchiveCourseCommandHandler archiveCourseCommandHandler;
    private final UpdateCourseCreditsCommandHandler updateCourseCreditsCommandHandler;
    private final UpdateCoursePrerequisitesCommandHandler updateCoursePrerequisitesCommandHandler;
    private final GetCourseDetailsQueryHandler getCourseDetailsQueryHandler;
    private final GetCourseStatisticsQueryHandler getCourseStatisticsQueryHandler;

    public CourseController(CreateCourseCommandHandler createCourseCommandHandler,
                            ArchiveCourseCommandHandler archiveCourseCommandHandler,
                            UpdateCourseCreditsCommandHandler updateCourseCreditsCommandHandler,
                            UpdateCoursePrerequisitesCommandHandler updateCoursePrerequisitesCommandHandler,
                            GetCourseDetailsQueryHandler getCourseDetailsQueryHandler,
                            GetCourseStatisticsQueryHandler getCourseStatisticsQueryHandler) {
        this.createCourseCommandHandler = createCourseCommandHandler;
        this.archiveCourseCommandHandler = archiveCourseCommandHandler;
        this.updateCourseCreditsCommandHandler = updateCourseCreditsCommandHandler;
        this.updateCoursePrerequisitesCommandHandler = updateCoursePrerequisitesCommandHandler;
        this.getCourseDetailsQueryHandler = getCourseDetailsQueryHandler;
        this.getCourseStatisticsQueryHandler = getCourseStatisticsQueryHandler;
    }

    @PostMapping
    public ResponseEntity<Long> createCourse(@RequestBody CreateCourseCommand command) {
        Long courseId = createCourseCommandHandler.handle(command);
        return ResponseEntity.created(URI.create("/api/v1/courses/" + courseId)).body(courseId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        archiveCourseCommandHandler.handle(new ArchiveCourseCommand(id));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/credits")
    public ResponseEntity<Void> updateCredits(@PathVariable Long id,
                                              @RequestBody UpdateCourseCreditsRequest request) {
        updateCourseCreditsCommandHandler.handle(new UpdateCourseCreditsCommand(id, request.credits()));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/prerequisites")
    public ResponseEntity<Void> updatePrerequisites(@PathVariable Long id,
                                                    @RequestBody UpdateCoursePrerequisitesRequest request) {
        updateCoursePrerequisitesCommandHandler.handle(
                new UpdateCoursePrerequisitesCommand(id, request.prerequisiteIds()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetCourseDetailsResponse> getCourseDetails(@PathVariable Long id) {
        GetCourseDetailsResponse response = getCourseDetailsQueryHandler.handle(new GetCourseDetailsQuery(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<GetCourseStatisticsResponse> getCourseStatistics(@PathVariable Long id) {
        GetCourseStatisticsResponse response = getCourseStatisticsQueryHandler
                .handle(new GetCourseStatisticsQuery(id));
        return ResponseEntity.ok(response);
    }

    public record UpdateCourseCreditsRequest(int credits) {}

    public record UpdateCoursePrerequisitesRequest(List<Long> prerequisiteIds) {}
}
