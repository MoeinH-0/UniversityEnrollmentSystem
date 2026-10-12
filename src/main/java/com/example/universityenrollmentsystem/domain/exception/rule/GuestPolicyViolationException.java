package com.example.universityenrollmentsystem.domain.exception.rule;

import com.example.universityenrollmentsystem.domain.exception.DomainException;

public class GuestPolicyViolationException extends DomainException {

    public GuestPolicyViolationException() {
        super("Guest students can only select guest-allowed courses.");
    }
}
