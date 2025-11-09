package com.taskmanagement.response;

import lombok.Data;

@Data
public class ApiResponse {
    private String message;
    private boolean status;
    
    public ApiResponse(String string, boolean b) {
        this.message = string;
        this.status = b;
    }

    public ApiResponse() {
        // Default constructor
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
}
