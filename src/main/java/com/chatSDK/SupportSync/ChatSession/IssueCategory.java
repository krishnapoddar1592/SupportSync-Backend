package com.chatSDK.SupportSync.ChatSession;

public enum IssueCategory {
    TECHNICAL("Technical Support"),
    BILLING("Billing & Payments"),
    PRODUCT("Product Information"),
    GENERAL("General Inquiry");

    private final String displayName;

    IssueCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}