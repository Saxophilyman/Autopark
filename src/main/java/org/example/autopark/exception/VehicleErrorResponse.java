package org.example.autopark.exception;

public class VehicleErrorResponse {
    private String message;
    private long timestamp;
    public VehicleErrorResponse(String message, long timestamp) {
        super();
        this.message = message;
        this.timestamp = timestamp;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
