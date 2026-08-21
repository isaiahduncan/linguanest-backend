package com.linguanest.backend.exercise;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateExerciseRequest(
        @NotBlank String language,
        @NotBlank String prompt,
        @NotNull Exercise.Difficulty difficulty) {
}
