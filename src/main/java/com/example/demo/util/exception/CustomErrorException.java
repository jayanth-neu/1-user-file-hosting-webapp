package com.example.demo.util.exception;

import org.springframework.http.HttpStatus;

/**
 * bipin : 2/6/22 : Created the class
 */
public class CustomErrorException extends RuntimeException{

    private HttpStatus status = null;

    private Object data = null;

    public CustomErrorException() {
    }

    public CustomErrorException(String message) {
        super(message);
    }

    public CustomErrorException(String message, HttpStatus status) {
        this(message);
        this.status = status;
    }
    public CustomErrorException(String message, HttpStatus status, Object data) {
        this(message, status);
        this.data = data;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
