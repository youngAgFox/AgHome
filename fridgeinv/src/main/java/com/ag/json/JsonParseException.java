package com.ag.json;

public class JsonParseException extends RuntimeException {
    public JsonParseException(String message) {
        super(message);
    }

    public JsonParseException(String message, Throwable t) {
        super(message, t);
    }
}
