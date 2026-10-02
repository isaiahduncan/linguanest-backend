package com.linguanest.backend.submission;

/** Mirrors the {@code submissions.submission_type} CHECK constraint. */
public enum SubmissionType {
    TEXT,
    /** Phase 2+: photo-based submission and grading. Not produced in Phase 1. */
    IMAGE
}
