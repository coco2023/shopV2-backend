package com.UmiUni.shop.model;

// ExitNotification class for deserializing the JSON request body
public class ExitNotification {
    private String exitReason;

    public String getExitReason() {
        return exitReason;
    }

    public void setExitReason(String exitReason) {
        this.exitReason = exitReason;
    }
}
