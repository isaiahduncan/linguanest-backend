package com.linguanest.backend.chat;

import java.util.UUID;

record QuestionDto(UUID id, int questionNumber, String promptText, String answerKey) {
}
