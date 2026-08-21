package com.linguanest.backend.exercise;

public record Exercise(String id, String language, String prompt, Difficulty difficulty) {

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
