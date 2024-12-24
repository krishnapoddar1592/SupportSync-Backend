package com.chatSDK.SupportSync.Repositories;

import com.chatSDK.SupportSync.messages.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find all messages by chat session ID
    List<Message> findByChatSessionId(Long chatSessionId);

    // Count the number of messages by chat session ID
     long countByChatSessionId(Long chatSessionId);
}