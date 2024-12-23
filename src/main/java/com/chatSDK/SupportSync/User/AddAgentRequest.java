package com.chatSDK.SupportSync.User;

import com.chatSDK.SupportSync.ChatSession.ChatSession;

public class AddAgentRequest {
    private AppUser user;
    private ChatSession sessionTemp;

    public AddAgentRequest(){

    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public ChatSession getSessionTemp() {
        return sessionTemp;
    }

    public void setSessionTemp(ChatSession sessionTemp) {
        this.sessionTemp = sessionTemp;
    }

    public AddAgentRequest(AppUser user, ChatSession sessionTemp) {
        this.user = user;
        this.sessionTemp = sessionTemp;
    }

    @Override
    public String toString() {
        return "AddAgentRequest{" +
                "user=" + user +
                ", sessionTemp=" + sessionTemp +
                '}';
    }
    // Getters and setters
}
