package com.linguanest.backend.exercise;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ExerciseController.class)
class ExerciseExceptionHandler {

    @ExceptionHandler(ExerciseNotFoundException.class)
    ResponseEntity<String> handleNotFound(ExerciseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
