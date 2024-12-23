package com.chatSDK.SupportSync.Repositories;

import com.chatSDK.SupportSync.messages.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}