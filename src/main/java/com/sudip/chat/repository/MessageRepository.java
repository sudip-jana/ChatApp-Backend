package com.sudip.chat.repository;

import com.sudip.chat.entity.Message;
import com.sudip.chat.model.MessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface MessageRepository
        extends JpaRepository<Message, Long> {

    List<Message> findBySenderAndReceiverOrSenderAndReceiverOrderByCreatedAtAsc(
            String sender1,
            String receiver1,
            String sender2,
            String receiver2
    );

    List<Message> findByReceiverAndStatusOrderByCreatedAtAsc(
            String receiver,
            MessageStatus status
    );

    @Query("""
        SELECT m FROM Message m
        WHERE (
            (m.sender = :user1 AND m.receiver = :user2)
            OR
            (m.sender = :user2 AND m.receiver = :user1)
        )
        AND (:beforeId IS NULL OR m.id < :beforeId)
        ORDER BY m.id DESC
        """)
    List<Message> findConversationBeforeId(
            @Param("user1") String user1,
            @Param("user2") String user2,
            @Param("beforeId") Long beforeId,
            Pageable pageable
    );

    @Query(value = """
    SELECT m.*
    FROM messages m
    INNER JOIN (
        SELECT MAX(id) AS id
        FROM messages
        WHERE sender = :me OR receiver = :me
        GROUP BY IF(sender = :me, receiver, sender)
    ) latest ON latest.id = m.id
    ORDER BY m.id DESC
    """, nativeQuery = true)
    List<Message> findLatestPerConversation(@Param("me") String me);
}
