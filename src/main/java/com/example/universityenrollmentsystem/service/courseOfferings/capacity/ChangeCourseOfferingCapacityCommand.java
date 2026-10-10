package com.example.universityenrollmentsystem.service.courseOfferings.capacity;

public record ChangeCourseOfferingCapacityCommand(Long courseOfferingId, int newCapacity) {
}
