package com.linguanest.backend.exercise;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    List<Exercise> listExercises() {
        return exerciseRepository.findAll();
    }

    Exercise getExercise(String id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
    }

    Exercise createExercise(String language, String prompt, Exercise.Difficulty difficulty) {
        Exercise exercise = new Exercise(UUID.randomUUID().toString(), language, prompt, difficulty);
        return exerciseRepository.save(exercise);
    }
}
