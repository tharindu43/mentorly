package com.example.mentorly.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    private int status;
    private String message;
    private long timestamp;
    private Map<String, String> errors;
}