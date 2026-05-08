package com.example.aeroship.models;

public class Booking {

    private String bookingId;

    private Boolean advancePaid = false;
    private String paymentStatus = "Pending";
    private String screenshotUrl;
    private long proofSubmittedAt;
    private String userId;
    private String userName;

    private int remainingAmount;
    private String userPhone;

    private String sector;
    private String level;
    private String duration;
    private String date;
    private String startTime;
    private String endTime;
    private String location;
    private int droneCount;

    private String droneId;
    private String assignedDrone;
    private String operatorId;

    private String assignedOperatorId;
    private String assignedOperatorName;
    private String assignedOperatorPhone;

    private int totalAmount;
    private String transactionId;

    private int advanceAmount;
    private long paymentTimestamp;
    private String notification;

    private String status;

    private long timestamp;
    private Boolean inventoryReleased = false;

    private String paymentStage;
    private String sectorId;
    private boolean remainingPaid = false;

    public Booking() {
    }

    public String getPaymentStage() {
        return paymentStage;
    }

    public void setPaymentStage(String paymentStage) {
        this.paymentStage = paymentStage;
    }

    public String getSectorId() {
        return sectorId;
    }

    public void setSectorId(String sectorId) {
        this.sectorId = sectorId;
    }

    public boolean isRemainingPaid() {
        return remainingPaid;
    }

    public void setRemainingPaid(boolean remainingPaid) {
        this.remainingPaid = remainingPaid;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public void setScreenshotUrl(String screenshotUrl) {
        this.screenshotUrl = screenshotUrl;
    }

    public long getProofSubmittedAt() {
        return proofSubmittedAt;
    }

    public void setProofSubmittedAt(long proofSubmittedAt) {
        this.proofSubmittedAt = proofSubmittedAt;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getSector() {
        return sector;
    }

    public String getLevel() {
        return level;
    }

    public int getRemainingAmount() {
        return remainingAmount;
    }

    public String getDuration() {
        return duration;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public int getDroneCount() {
        return droneCount;
    }

    public String getDroneId() {
        return droneId;
    }

    public String getAssignedDrone() {
        return assignedDrone;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getAssignedOperatorId() {
        return assignedOperatorId;
    }

    public String getAssignedOperatorName() {
        return assignedOperatorName;
    }

    public String getAssignedOperatorPhone() {
        return assignedOperatorPhone;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Boolean getAdvancePaid() {
        return advancePaid;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public int getAdvanceAmount() {
        return advanceAmount;
    }

    public void setRemainingAmount(int remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public long getPaymentTimestamp() {
        return paymentTimestamp;
    }

    public String getNotification() {
        return notification;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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

    public void setSector(String sector) {
        this.sector = sector;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDroneCount(int droneCount) {
        this.droneCount = droneCount;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public void setAssignedDrone(String assignedDrone) {
        this.assignedDrone = assignedDrone;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public void setAssignedOperatorId(String assignedOperatorId) {
        this.assignedOperatorId = assignedOperatorId;
    }

    public void setAssignedOperatorName(String assignedOperatorName) {
        this.assignedOperatorName = assignedOperatorName;
    }

    public void setAssignedOperatorPhone(String assignedOperatorPhone) {
        this.assignedOperatorPhone = assignedOperatorPhone;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAdvancePaid(Boolean advancePaid) {
        this.advancePaid = advancePaid;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setAdvanceAmount(int advanceAmount) {
        this.advanceAmount = advanceAmount;
    }

    public void setPaymentTimestamp(long paymentTimestamp) {
        this.paymentTimestamp = paymentTimestamp;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public Boolean getInventoryReleased() {
        return inventoryReleased != null && inventoryReleased;
    }

    public void setInventoryReleased(Boolean inventoryReleased) {
        this.inventoryReleased = inventoryReleased;
    }
}