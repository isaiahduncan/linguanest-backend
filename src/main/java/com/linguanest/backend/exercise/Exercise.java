package com.linguanest.backend.exercise;

import com.linguanest.backend.chat.Chat;
import com.linguanest.backend.common.BaseEntity;
import com.linguanest.backend.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One specific worksheet instance given to one specific user, in one specific chat.
 *
 * <p>{@code userId} is a deliberate, acceptable redundancy with {@code chat.userId} - kept
 * directly on this table to avoid a join for "get all exercises for user X" (see
 * backend-api-spec.md).
 */
@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
public class Exercise extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "language", length = 50, nullable = false)
    private String language;

    @Column(name = "topic", length = 255, nullable = false)
    private String topic;
}
