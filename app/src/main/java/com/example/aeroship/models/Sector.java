package com.example.aeroship.models;

public class Sector {

    private String sectorId;
    private String name;
    private String imageUrl;
    private String status;
    private long createdAt;

    public Sector() {
    }

    public Sector(String sectorId,
                  String name,
                  String imageUrl,
                  String status,
                  long createdAt) {

        this.sectorId = sectorId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getSectorId() {
        return sectorId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setSectorId(String sectorId) {
        this.sectorId = sectorId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    public boolean isPaused() {
        return "Paused".equalsIgnoreCase(status);
    }

    public boolean isDeleted() {
        return "Deleted".equalsIgnoreCase(status);
    }
}