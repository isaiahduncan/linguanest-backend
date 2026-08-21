package com.linguanest.backend.exercise;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

@Repository
class InMemoryExerciseRepository implements ExerciseRepository {

    private final Map<String, Exercise> exercises = new ConcurrentHashMap<>();

    @Override
    public List<Exercise> findAll() {
        return List.copyOf(exercises.values());
    }

    @Override
    public Optional<Exercise> findById(String id) {
        return Optional.ofNullable(exercises.get(id));
    }

    @Override
    public Exercise save(Exercise exercise) {
        exercises.put(exercise.id(), exercise);
        return exercise;
    }
}
