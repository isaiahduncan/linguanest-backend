package com.linguanest.backend.chat;

import java.time.Instant;
import java.util.UUID;

import com.linguanest.backend.submission.SubmissionStatus;
import com.linguanest.backend.submission.SubmissionType;

record SubmissionTimelineEntry(
        UUID submissionId,
        UUID exerciseId,
        SubmissionType submissionType,
        SubmissionStatus status,
        String rawTextInput,
        Instant createdAt,
        GradedResultDto gradedResult) implements ChatTimelineEntry {
}
