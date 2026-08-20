package com.example.study11.common.model;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一错误响应体
 */
@Data
public class ErrorResponse implements Serializable {

    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private Object details;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse of(HttpStatus status, String message) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(status.value());
        response.setError(status.getReasonPhrase());
        response.setMessage(message);
        return response;
    }

    public static ErrorResponse of(HttpStatus status, String message, Object details) {
        ErrorResponse response = of(status, message);
        response.setDetails(details);
        return response;
    }

    public static ErrorResponse of(HttpStatus status, Object details) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(status.value());
        response.setError(status.getReasonPhrase());
        response.setDetails(details);
        return response;
    }
}
