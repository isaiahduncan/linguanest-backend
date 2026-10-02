package com.linguanest.backend.submission;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GradedResultRepository extends JpaRepository<GradedResult, UUID> {

    List<GradedResult> findBySubmissionIdIn(List<UUID> submissionIds);
}
