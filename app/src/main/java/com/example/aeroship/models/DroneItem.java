package com.example.aeroship.models;

public class DroneItem {
    private String droneId;
    private int count;

    public DroneItem() {
    }

    public DroneItem(String droneId, int count) {
        this.droneId = droneId;
        this.count = count;
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}