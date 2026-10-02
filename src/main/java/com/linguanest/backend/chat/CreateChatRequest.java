package com.linguanest.backend.chat;

/** Request body for {@code POST /chats}. {@code title} is optional; the body itself may be omitted entirely. */
record CreateChatRequest(String title) {
}
