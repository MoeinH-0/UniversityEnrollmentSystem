package com.example.universityenrollmentsystem.service.courseOfferings.guestpolicy;

public record ChangeCourseOfferingGuestPolicyCommand(Long courseOfferingId, boolean guestAllowed) {
}
