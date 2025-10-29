package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
    Optional<ConversationMember> findByConversationConversationIdAndAccountAccountId(Long conversationId, Long accountId);

    List<ConversationMember> findByConversationConversationId(Long conversationId);

    List<ConversationMember> findByConversationConversationIdAndIsActiveTrue(Long conversationId);

    @Query("""
                SELECT cm FROM ConversationMember cm
                WHERE cm.account.accountId = :accountId
                AND cm.conversation.workspace.workspaceId = :workspaceId
                AND cm.isActive = true
            """)
    List<ConversationMember> findActiveMembersInWorkspaceByAccountId(
            @Param("workspaceId") Long workspaceId,
            @Param("accountId") Long accountId
    );


    //    boolean existsByConversationConversationIdAndAccountAccountId(Long conversationId, Long accountId);
    boolean existsByConversationConversationIdAndAccountAccountIdAndIsActiveTrue(Long conversationId, Long accountId);

}