package com.linguanest.backend.exercise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ExerciseServiceTest {

    private final ExerciseRepository exerciseRepository = new InMemoryExerciseRepository();
    private final ExerciseService exerciseService = new ExerciseService(exerciseRepository);

    @Test
    void createsAndRetrievesAnExercise() {
        Exercise created = exerciseService.createExercise("es", "Translate 'hello'", Exercise.Difficulty.BEGINNER);

        Exercise found = exerciseService.getExercise(created.id());

        assertThat(found).isEqualTo(created);
    }

    @Test
    void throwsWhenExerciseIsMissing() {
        assertThatThrownBy(() -> exerciseService.getExercise("missing"))
                .isInstanceOf(ExerciseNotFoundException.class);
    }
}
