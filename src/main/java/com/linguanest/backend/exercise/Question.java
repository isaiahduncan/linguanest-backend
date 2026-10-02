package com.linguanest.backend.exercise;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single question belonging to one {@link Exercise} (prompt text, answer key, question
 * number). Pure content - no user-specific data, and strictly a child of {@code exercises};
 * nothing else references it.
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @Column(name = "question_number", nullable = false)
    private int questionNumber;

    @Column(name = "prompt_text", nullable = false)
    private String promptText;

    @Column(name = "answer_key", nullable = false)
    private String answerKey;
}
