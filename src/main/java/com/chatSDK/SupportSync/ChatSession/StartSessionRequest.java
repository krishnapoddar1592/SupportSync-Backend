package com.chatSDK.SupportSync.ChatSession;

import com.chatSDK.SupportSync.User.AppUser;

public class StartSessionRequest {
    private AppUser user;
    private IssueCategory category;

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public IssueCategory getCategory() {
        return category;
    }

    public void setCategory(IssueCategory category) {
        this.category = category;
    }
}
