package com.ag.json;

public class JsonInitializeException extends RuntimeException {
    public JsonInitializeException(String message) {
        super(message);
    }

    public JsonInitializeException(String message, Throwable t) {
        super(message, t);
    }
}
