package com.example.aeroship.models;

public class NotificationModel {

    private String title;
    private String message;
    private long timestamp;
    private boolean read;

    public NotificationModel() {
    }

    public NotificationModel(String title, String message,
                             long timestamp, boolean read) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getTitle() {
        if (title == null || title.trim().isEmpty()) {
            return "Notification";
        }
        return title;
    }

    public String getMessage() {
        if (message == null || message.trim().isEmpty()) {
            return "No message available";
        }
        return message;
    }

    public long getTimestamp() {
        return timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    public boolean isRead() {
        return read;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}