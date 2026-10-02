package com.linguanest.backend.submission;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.linguanest.backend.exercise.Exercise;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One attempt by a user to answer a specific {@link Exercise} (typed text in Phase 1; photo in
 * Phase 2+). Tracks processing status. Multiple submissions per exercise are allowed
 * (retries/resubmissions).
 */
@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
public class Submission {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_type", length = 20)
    private SubmissionType submissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SubmissionStatus status;

    /** Phase 2+: set only for IMAGE submissions. */
    @Column(name = "image_s3_key", length = 512)
    private String imageS3Key;

    @Column(name = "raw_text_input")
    private String rawTextInput;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
