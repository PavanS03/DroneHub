package com.example.aeroship.models;

public class SupportMessage {

    private String messageId;
    private String userId;
    private String userName;
    private String userPhone;

    private String userMessage;
    private String adminReply;

    private long timestamp;
    private String status;

    public SupportMessage() {
    }

    public SupportMessage(String messageId,
                          String userId,
                          String userName,
                          String userPhone,
                          String userMessage,
                          String adminReply,
                          long timestamp,
                          String status) {

        this.messageId = messageId;
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userMessage = userMessage;
        this.adminReply = adminReply;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getMessageId() {
        return messageId != null ? messageId : "";
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public String getUserName() {
        if (userName == null || userName.trim().isEmpty()
                || userName.equalsIgnoreCase("unknown")) {
            return "Customer";
        }
        return userName;
    }

    public String getUserPhone() {
        if (userPhone == null
                || userPhone.trim().isEmpty()
                || userPhone.equals("0000000000")) {
            return "";
        }
        return userPhone;
    }

    public String getUserMessage() {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "No message";
        }
        return userMessage;
    }

    public String getAdminReply() {
        return adminReply != null ? adminReply : "";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        if (status == null || status.trim().isEmpty()) {
            return "Pending";
        }
        return status;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}