package com.linguanest.backend.exercise;

class ExerciseNotFoundException extends RuntimeException {

    ExerciseNotFoundException(String id) {
        super("Exercise not found: " + id);
    }
}
