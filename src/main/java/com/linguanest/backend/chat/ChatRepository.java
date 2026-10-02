package com.linguanest.backend.chat;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    List<Chat> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
