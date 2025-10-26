package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
    Optional<ConversationMember> findByConversationConversationIdAndAccountAccountId(Long conversationId, Long accountId);

    List<ConversationMember> findByConversationConversationId(Long conversationId);

    boolean existsByConversationConversationIdAndAccountAccountId(Long conversationId, Long accountId);
}