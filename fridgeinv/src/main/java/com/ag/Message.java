package com.ag;

import java.util.HashMap;
import java.util.Map;

public class Message {
    private Map<String, String> parameters = new HashMap<>();
    private String command;

    public Message(String message) {
        if (message.indexOf("?") < 0) {
            command = message;
        } else {
            String[] parts = message.split("\\?", 2);
            command = parts[0];
            String[] paramPairs = parts[1].split(";");
            for (String pair : paramPairs) {
                if (pair.indexOf("=") < 0) {
                    continue;
                }
                String[] param = pair.split("=", 2);
                parameters.put(param[0], param[1]);
            }
        }
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return String.format("Command: '%s' Parameters: %s", command, parameters.toString());
    }

}
