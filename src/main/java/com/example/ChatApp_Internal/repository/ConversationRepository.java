package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.Conversation;
import com.example.ChatApp_Internal.entity.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByWorkspaceWorkspaceIdAndIsArchivedFalse(Long workspaceId);

    List<Conversation> findByWorkspaceWorkspaceIdAndTypeAndIsArchivedFalse(Long workspaceId, ConversationType type);

//    @Query("SELECT c FROM Conversation c JOIN c.members m WHERE m.account.accountId = :accountId AND c.workspace.workspaceId = :workspaceId AND c.isArchived = false")
//    List<Conversation> findByWorkspaceIdAndMemberAccountId(Long workspaceId, Long accountId);

    @Query("""
                SELECT c FROM Conversation c
                JOIN c.members m
                WHERE m.account.accountId = :accountId
                  AND c.workspace.workspaceId = :workspaceId
                  AND c.isArchived = false
                  AND m.isActive = true
            """)
    List<Conversation> findByWorkspaceIdAndMemberAccountId(
            @Param("workspaceId") Long workspaceId,
            @Param("accountId") Long accountId
    );

}
