package com.hellofresh.service.exception;

public class InvalidArgumentFormatException extends RuntimeException {
    public InvalidArgumentFormatException(String message) {
        super(message);
    }
}