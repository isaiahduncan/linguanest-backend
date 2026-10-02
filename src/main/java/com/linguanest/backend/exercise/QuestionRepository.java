package com.linguanest.backend.exercise;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByExerciseIdInOrderByQuestionNumberAsc(List<UUID> exerciseIds);
}
