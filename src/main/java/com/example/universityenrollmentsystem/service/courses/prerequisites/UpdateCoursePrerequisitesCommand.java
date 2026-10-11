package com.example.universityenrollmentsystem.service.courses.prerequisites;
import java.util.List;

public record UpdateCoursePrerequisitesCommand(Long courseId, List<Long> prerequisiteIds) {}
