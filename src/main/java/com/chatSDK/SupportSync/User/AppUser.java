package com.chatSDK.SupportSync.User;

import jakarta.persistence.*;

@Entity
public class AppUser {
    @Id
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private UserRole role; // AGENT or CUSTOMER

    public AppUser() {
        // Default constructor for JPA and Jackson
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }

    public AppUser(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
