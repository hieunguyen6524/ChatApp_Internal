package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.RefreshContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshContextRepository extends JpaRepository<RefreshContext, Long> {
    Optional<RefreshContext> findByContextId(String contextId);

    @Modifying
    @Transactional // ⚠️ cần thêm khi update/delete
    @Query("UPDATE RefreshContext r SET r.revoked = true WHERE r.account.accountId = :accountId AND r.revoked = false")
    void revokeAllActiveByAccountId(@Param("accountId") Long accountId);
    

    @Modifying
    @Query("UPDATE RefreshContext r SET r.revoked = true WHERE r.account.accountId = :accountId")
    void revokeAllByAccountId(Long accountId);
}
