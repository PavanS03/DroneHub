package com.example.aeroship.models;

public class User {

    private String userId;
    private String name;
    private String email;
    private String phone;
    private String role;

    private String password;

    private long createdAt;
    private boolean active;

    public User() {}

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public String getName() {
        if (name == null || name.isEmpty()) return "Unknown";
        return name;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public String getRole() {
        return role != null ? role : "";
    }

    public String getPassword() {
        return password != null ? password : "";
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}