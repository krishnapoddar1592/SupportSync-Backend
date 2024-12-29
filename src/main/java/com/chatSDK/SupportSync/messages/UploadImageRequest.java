package com.chatSDK.SupportSync.messages;

import org.springframework.web.multipart.MultipartFile;

public  class UploadImageRequest {
    private MultipartFile file;
    private Long userId;

    // Getters and Setters
    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
