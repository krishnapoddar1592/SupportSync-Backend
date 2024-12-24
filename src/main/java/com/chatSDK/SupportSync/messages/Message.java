package com.chatSDK.SupportSync.messages;

import com.chatSDK.SupportSync.ChatSession.ChatSession;
import com.chatSDK.SupportSync.User.AppUser;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private ChatSession chatSession;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private AppUser sender; // The sender (customer or agent)

    @Column(nullable = false)
    private String content;

    private LocalDateTime timestamp;

    @Column(nullable = true)
    private String imageUrl; // Path to the stored image or Base64 string

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
// Getters and Setters


    public ChatSession getChatSession() {
        return chatSession;
    }

    public void setChatSession(ChatSession chatSession) {
        this.chatSession = chatSession;
    }

    public AppUser getSender() {
        return sender;
    }

    public void setSender(AppUser sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", chatSession=" + chatSession +
                ", sender=" + sender +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    public Message(String content, AppUser sender, ChatSession chatSession, long timestamp) {
        this.content = content;
        this.sender = sender;
        this.chatSession = chatSession;
        this.timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    public Message(ChatSession chatSession, AppUser sender, String content, long timestamp, String imageUrl) {
        this.chatSession = chatSession;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        this.imageUrl = imageUrl;
    }
    public Message(){

    }
}

