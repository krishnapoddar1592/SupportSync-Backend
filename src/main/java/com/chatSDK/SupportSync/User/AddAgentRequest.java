package com.chatSDK.SupportSync.User;

import com.chatSDK.SupportSync.ChatSession.ChatSession;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddAgentRequest {
    @JsonProperty("agent")
    private AppUser agent;
    private ChatSession sessionTemp;

    public AddAgentRequest(){

    }

    public AppUser getUser() {
        return agent;
    }

    public void setUser(AppUser agent) {
        this.agent = agent;
    }

    public ChatSession getSessionTemp() {
        return sessionTemp;
    }

    public void setSessionTemp(ChatSession sessionTemp) {
        this.sessionTemp = sessionTemp;
    }

    public AddAgentRequest(AppUser agent, ChatSession sessionTemp) {
        this.agent = agent;
        this.sessionTemp = sessionTemp;
    }

    @Override
    public String toString() {
        return "AddAgentRequest{" +
                "agent=" + agent +
                ", sessionTemp=" + sessionTemp +
                '}';
    }
    // Getters and setters
}
