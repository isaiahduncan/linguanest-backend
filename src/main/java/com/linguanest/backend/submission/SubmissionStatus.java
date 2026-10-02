package com.linguanest.backend.submission;

/** Mirrors the {@code submissions.status} CHECK constraint. */
public enum SubmissionStatus {
    PENDING,
    PROCESSING,
    GRADED,
    FAILED
}
