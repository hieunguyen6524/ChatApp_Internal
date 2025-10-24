package com.example.ChatApp_Internal.repository;


import com.example.ChatApp_Internal.entity.TokenType;
import com.example.ChatApp_Internal.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndType(String token, TokenType type);

}
