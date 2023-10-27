package com.ag.json;

public class JsonInvalidLiteralException extends RuntimeException {
    public JsonInvalidLiteralException(String message) {
        super(message);
    }

    public JsonInvalidLiteralException(String message, Throwable t) {
        super(message, t);
    }
}
