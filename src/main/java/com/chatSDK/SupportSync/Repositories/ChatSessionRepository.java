package com.chatSDK.SupportSync.Repositories;

import com.chatSDK.SupportSync.ChatSession.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
}
