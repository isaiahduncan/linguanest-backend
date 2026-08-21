package com.linguanest.backend.exercise;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository {

    List<Exercise> findAll();

    Optional<Exercise> findById(String id);

    Exercise save(Exercise exercise);
}
