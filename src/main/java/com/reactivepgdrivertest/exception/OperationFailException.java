package com.reactivepgdrivertest.exception;

public class OperationFailException extends RuntimeException {

    private String type;

    public OperationFailException(String type, String message) {
        super(message);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
