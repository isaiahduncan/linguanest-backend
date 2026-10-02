package com.linguanest.backend.chat;

import java.time.Instant;
import java.util.UUID;

/** Minimal representation of a chat, used for both the create response and the chat list. */
record ChatSummary(UUID id, String title, Instant createdAt, Instant updatedAt) {

    static ChatSummary from(Chat chat) {
        return new ChatSummary(chat.getId(), chat.getTitle(), chat.getCreatedAt(), chat.getUpdatedAt());
    }
}
