package com.chatSDK.SupportSync.ChatSession;

import com.chatSDK.SupportSync.User.AppUser;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user; // The customer

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private AppUser agent; // The support agent

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public IssueCategory getIssueCategory() {
        return issueCategory;
    }

    public void setIssueCategory(IssueCategory issueCategory) {
        this.issueCategory = issueCategory;
    }

    private IssueCategory issueCategory;


    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public AppUser getAgent() {
        return agent;
    }

    public void setAgent(AppUser agent) {
        this.agent = agent;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAt), ZoneId.systemDefault());
    }

    @Override
    public String toString() {
        return "ChatSession{" +
                "id=" + id +
                ", user=" + user +
                ", agent=" + agent +
                ", startedAt=" + startedAt +
                ", endedAt=" + endedAt +
                ", issueCategory="+issueCategory+
                '}';
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }


    public Long getId() {
        return this.id;
    }
}
