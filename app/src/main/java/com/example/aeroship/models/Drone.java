package com.example.aeroship.models;

public class Drone {
    private String droneId;
    private String sector;
    private String level;
    private int hourlyRate;
    private String status;
    private long createdAt;

    public Drone() {
    }

    public Drone(String droneId, String sector, String level, int hourlyRate, String status) {
        this.droneId = droneId;
        this.sector = sector;
        this.level = level;
        this.hourlyRate = hourlyRate;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
    }

    public String getDroneId() { return droneId; }
    public void setDroneId(String droneId) { this.droneId = droneId; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(int hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}