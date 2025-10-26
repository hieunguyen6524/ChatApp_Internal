package com.example.ChatApp_Internal.repository;

import com.example.ChatApp_Internal.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Long conversationId, Pageable pageable);

    List<Message> findByConversationConversationIdAndParentMessageIdAndIsDeletedFalse(
            Long conversationId, Long parentMessageId);

    @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId " +
            "AND m.isDeleted = false AND m.content LIKE %:keyword% ORDER BY m.createdAt DESC")
    List<Message> searchInConversation(Long conversationId, String keyword);

    Long countByConversationConversationIdAndCreatedAtGreaterThan(Long conversationId, Long timestamp);
}
