package com.linguanest.backend.chat;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
class ChatController {

    private final ChatService chatService;

    @PostMapping
    ResponseEntity<ChatSummary> createChat(@RequestBody(required = false) CreateChatRequest request) {
        String title = request != null ? request.title() : null;
        ChatSummary created = chatService.createChat(title);
        // Points at /messages, not a bare /chats/{id} - there's no GET mapping for the latter
        // (only POST /chats, GET /chats, and GET /chats/{chatId}/messages exist), so a client
        // following this Location header would otherwise get a 404 on standard REST behavior.
        return ResponseEntity.created(URI.create("/chats/" + created.id() + "/messages")).body(created);
    }

    @GetMapping
    List<ChatSummary> listChats() {
        return chatService.listChats();
    }

    @GetMapping("/{chatId}/messages")
    ChatMessagesResponse getMessages(@PathVariable UUID chatId) {
        return chatService.getMessages(chatId);
    }
}
