package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.RefreshContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshContextRepository extends JpaRepository<RefreshContext, Long> {
    Optional<RefreshContext> findByContextId(String contextId);

    @Modifying
    @Query("DELETE FROM RefreshContext r WHERE r.expiryDate < :currentTime")
    void deleteExpiredTokens(Long currentTime);

    @Modifying
    @Query("UPDATE RefreshContext r SET r.revoked = true WHERE r.account.accountId = :accountId")
    void revokeAllByAccountId(Long accountId);
}
