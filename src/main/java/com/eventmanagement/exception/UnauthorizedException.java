// src/main/java/com/eventmanagement/exception/UnauthorizedException.java
package com.eventmanagement.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}