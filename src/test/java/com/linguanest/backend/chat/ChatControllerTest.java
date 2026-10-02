package com.linguanest.backend.chat;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void createChatReturns201WithTheCreatedChatsSummary() throws Exception {
        UUID chatId = UUID.randomUUID();
        when(chatService.createChat("Spanish practice"))
                .thenReturn(new ChatSummary(chatId, "Spanish practice", Instant.now(), Instant.now()));

        mockMvc.perform(post("/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Spanish practice"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Spanish practice")));
    }

    @Test
    void createChatWithNoBodyStillCreatesAChatWithNoTitle() throws Exception {
        UUID chatId = UUID.randomUUID();
        when(chatService.createChat(null))
                .thenReturn(new ChatSummary(chatId, null, Instant.now(), Instant.now()));

        mockMvc.perform(post("/chats"))
                .andExpect(status().isCreated());
    }

    @Test
    void listChatsReturnsWhateverTheServiceProvides() throws Exception {
        when(chatService.listChats()).thenReturn(List.of(
                new ChatSummary(UUID.randomUUID(), "Chat A", Instant.now(), Instant.now())));

        mockMvc.perform(get("/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Chat A")));
    }

    @Test
    void getMessagesReturns404WithACleanErrorBodyForAnUnknownChat() throws Exception {
        UUID unknownChatId = UUID.randomUUID();
        when(chatService.getMessages(unknownChatId)).thenThrow(new ChatNotFoundException(unknownChatId));

        mockMvc.perform(get("/chats/{chatId}/messages", unknownChatId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Chat not found: " + unknownChatId)));
    }

    @Test
    void getMessagesReturnsTheMergedTimelineForAKnownChat() throws Exception {
        UUID chatId = UUID.randomUUID();
        when(chatService.getMessages(any(UUID.class)))
                .thenReturn(new ChatMessagesResponse(chatId, List.of()));

        mockMvc.perform(get("/chats/{chatId}/messages", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId", is(chatId.toString())));
    }
}
