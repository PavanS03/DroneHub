package com.example.aeroship.models;

public class Payment {

    private String paymentId;
    private String userId;
    private String userName;
    private String amount;
    private String status;
    private long timestamp;

    public Payment() {}

    public Payment(String paymentId, String userId,
                   String userName, String amount,
                   String status, long timestamp) {

        this.paymentId = paymentId;
        this.userId = userId;
        this.userName = userName;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getAmount() { return amount; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
}