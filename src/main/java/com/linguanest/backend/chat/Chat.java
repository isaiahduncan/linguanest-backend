package com.linguanest.backend.chat;

import java.time.Instant;

import org.hibernate.annotations.UpdateTimestamp;

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
 * Groups a sequence of {@link com.linguanest.backend.exercise.Exercise Exercise}s into a
 * conversation thread for the chat-list/chat-detail mobile UI. Purely a UI/UX grouping construct -
 * holds no exercise content or grading logic itself.
 */
@Entity
@Table(name = "chats")
@Getter
@Setter
@NoArgsConstructor
public class Chat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title", length = 255)
    private String title;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
