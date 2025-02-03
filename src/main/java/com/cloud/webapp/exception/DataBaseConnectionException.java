package com.cloud.webapp.exception;


public class DataBaseConnectionException extends RuntimeException {
    public DataBaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}