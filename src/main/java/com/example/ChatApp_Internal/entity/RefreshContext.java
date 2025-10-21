package com.example.ChatApp_Internal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_contexts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshContext {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "context_id", unique = true, nullable = false, length = 64)
    private String contextId;

    @Column(name = "expiry_date", nullable = false)
    private Long expiryDate;

    @Column(nullable = false)
    private Boolean revoked = false;

    @Column(name = "created_at")
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
    }
}
