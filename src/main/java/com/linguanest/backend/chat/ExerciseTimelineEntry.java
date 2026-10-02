package com.linguanest.backend.chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

record ExerciseTimelineEntry(
        UUID exerciseId,
        String language,
        String topic,
        Instant createdAt,
        List<QuestionDto> questions) implements ChatTimelineEntry {
}
