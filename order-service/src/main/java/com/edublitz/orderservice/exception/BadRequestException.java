package com.edublitz.orderservice.exception;
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
