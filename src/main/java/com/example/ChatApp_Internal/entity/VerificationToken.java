package com.example.ChatApp_Internal.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(unique = true, nullable = false, length = 100)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TokenType type;

    @Column(name = "email_target", length = 100)
    private String emailTarget;

    @Column(name = "expiry_date", nullable = false)
    private Long expiryDate;

    @Column(nullable = false)
    private Boolean used = false;

    @Column(name = "created_at")
    private Long createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
    }
}
