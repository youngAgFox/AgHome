package com.ag.database;

public class NotUniqueParameterException extends RuntimeException {

    public NotUniqueParameterException(String message) {
        super(message);
    }

    public NotUniqueParameterException(String message, String parameter) {
        super("The parameter '" + parameter + "' is not unique: " + message);
    }
}
