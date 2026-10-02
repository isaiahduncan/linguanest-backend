package com.linguanest.backend.submission;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findByExerciseIdInOrderByCreatedAtAsc(List<UUID> exerciseIds);
}
