package com.example.ChatApp_Internal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conversation_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "is_channel_admin")
    private Boolean isChannelAdmin = false;

    @Column(name = "joined_at")
    private Long joinedAt;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "last_read_at")
    private Long lastReadAt;

    @Column(name = "is_notif_enabled")
    private Boolean isNotifEnabled = true;

    @PrePersist
    protected void onCreate() {
        joinedAt = System.currentTimeMillis();
    }
}
