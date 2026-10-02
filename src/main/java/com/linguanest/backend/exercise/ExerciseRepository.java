package com.linguanest.backend.exercise;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    List<Exercise> findByChatIdOrderByCreatedAtAsc(UUID chatId);
}
