package com.example.aeroship.models;

public class Operator {

    private String id;
    private String userId;
    private String name;
    private String phone;
    private String email;
    private String password;
    private String role;
    private String status;
    private boolean active;

    private String address;
    private String gender;
    private String age;
    private String alternateNumber;
    private long createdAt;

    public Operator() {
    }

    public String getId() {
        return id != null ? id : "";
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public String getName() {
        return name != null ? name : "Unknown";
    }

    public String getPhone() {
        return phone != null ? phone : "-";
    }

    public String getEmail() {
        return email != null ? email : "-";
    }

    public String getPassword() {
        return password != null ? password : "-";
    }

    public String getRole() {
        return role != null ? role : "operator";
    }

    public String getStatus() {
        if (status == null) return "available";
        return status.trim().toLowerCase();
    }

    public boolean isActive() {
        return active;
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    public String getGender() {
        return gender != null ? gender : "";
    }

    public String getAge() {
        return age != null ? age : "";
    }

    public String getAlternateNumber() {
        return alternateNumber != null ? alternateNumber : "";
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStatus(String status) {
        if (status == null) {
            this.status = "available";
        } else {
            this.status = status.trim().toLowerCase();
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setAlternateNumber(String alternateNumber) {
        this.alternateNumber = alternateNumber;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAvailable() {
        return getStatus().equals("available");
    }

    public boolean isBusy() {
        return getStatus().equals("busy");
    }

    public String getDisplayStatus() {
        if (isAvailable()) {
            return "Available ✅";
        } else {
            return "Busy ❌";
        }
    }
}