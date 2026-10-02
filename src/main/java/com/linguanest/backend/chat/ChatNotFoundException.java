package com.linguanest.backend.chat;

import java.util.UUID;

class ChatNotFoundException extends RuntimeException {

    ChatNotFoundException(UUID id) {
        super("Chat not found: " + id);
    }
}
