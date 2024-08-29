package com.servxglobal.tms.adminservice.dto;

public class SuccessandMessageDto {
    private boolean success;
    private String message;
    private String Data;
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getData() { return Data;}
    public void setData(String data) {
        this.Data = data;
    }
}
