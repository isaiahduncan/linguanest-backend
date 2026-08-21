package com.linguanest.backend.exercise;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    List<Exercise> listExercises() {
        return exerciseService.listExercises();
    }

    @GetMapping("/{id}")
    Exercise getExercise(@PathVariable String id) {
        return exerciseService.getExercise(id);
    }

    @PostMapping
    ResponseEntity<Exercise> createExercise(@Valid @RequestBody CreateExerciseRequest request) {
        Exercise created = exerciseService.createExercise(
                request.language(), request.prompt(), request.difficulty());
        return ResponseEntity.created(URI.create("/api/exercises/" + created.id())).body(created);
    }
}
