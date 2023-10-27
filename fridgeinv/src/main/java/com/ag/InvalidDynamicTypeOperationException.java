package com.ag;

public class InvalidDynamicTypeOperationException extends RuntimeException {
    
    public InvalidDynamicTypeOperationException(String message) {
        super(message);
    }

    public InvalidDynamicTypeOperationException(String message, Throwable t) {
        super(message, t);
    }
}
