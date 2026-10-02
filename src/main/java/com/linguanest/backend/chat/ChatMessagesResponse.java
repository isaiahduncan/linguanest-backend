package com.linguanest.backend.chat;

import java.util.List;
import java.util.UUID;

/** Response body for {@code GET /chats/{chatId}/messages}. */
record ChatMessagesResponse(UUID chatId, List<ChatTimelineEntry> messages) {
}
