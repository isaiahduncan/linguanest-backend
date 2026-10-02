package com.linguanest.backend.chat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

record GradedResultDto(
        UUID id,
        Integer overallScore,
        String feedbackMarkdown,
        Map<String, Object> detailedCorrections,
        Instant createdAt) {
}
