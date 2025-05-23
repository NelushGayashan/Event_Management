// src/main/java/com/eventmanagement/exception/BadRequestException.java
package com.eventmanagement.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}