package com.example.aeroship.models;

public class DronePackage {

    private String packageId;
    private String sectorId;
    private String level;
    private int availableCount;
    private boolean available;

    private String packageName;
    private String features;

    private int pricePerHour;

    public DronePackage() {
    }

    public DronePackage(String packageName,
                        String features,
                        int pricePerHour) {

        this.packageName = packageName;
        this.features = features;
        this.pricePerHour = pricePerHour;
    }

    public DronePackage(String packageId,
                        String sectorId,
                        String level,
                        int availableCount,
                        int pricePerHour,
                        boolean available) {

        this.packageId = packageId;
        this.sectorId = sectorId;
        this.level = level;
        this.availableCount = availableCount;
        this.pricePerHour = pricePerHour;
        this.available = available;
    }

    public String getPackageId() {
        return packageId;
    }

    public String getSectorId() {
        return sectorId;
    }

    public String getLevel() {
        return level;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFeatures() {
        return features;
    }

    public int getPricePerHour() {
        return pricePerHour;
    }

    public String getId() {
        return packageId;
    }

    public boolean isActive() {
        return available;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public void setSectorId(String sectorId) {
        this.sectorId = sectorId;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public void setPricePerHour(int pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public void setActive(boolean active) {
        this.available = active;
    }
}