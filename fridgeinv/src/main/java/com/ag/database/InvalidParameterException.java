package com.ag.database;

import java.util.Set;

public class InvalidParameterException extends RuntimeException {

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Set<String> parameters) {
        super(createInvalidParamsErrorMessage(message, parameters));
    }

    private static String createInvalidParamsErrorMessage(String message, Set<String> missingReqParams) {
        StringBuilder sb = new StringBuilder(message + ": Invalid parameters: ");
        for (String reqParam : missingReqParams) {
            sb.append("<").append(reqParam).append(">, ");
        }
        // truncate the ', ' at the end of the built String
        if (!missingReqParams.isEmpty()) {
            sb.setLength(sb.length() - 2);
        } else {
            sb.append("[No invalid parameters]");
        }
        return sb.toString();
    }
}
