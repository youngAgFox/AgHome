package com.ag;

import java.util.HashMap;
import java.util.Map;

public class Message {
    private final Map<String, String> parameters;
    private String command;

    public Message(String command) {
        this(command, new HashMap<>());
    }

    public Message(String command, Map<String, String> parameters) {
        this.command = command;
        this.parameters = parameters;
    }

    public static Message parseMessage(String data) {
        String command;
        Map<String, String> parameters = new HashMap<>();
        if (data.indexOf("?") < 0) {
            command = data;
        } else {
            String[] parts = data.split("\\?", 2);
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
        return new Message(command, parameters);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getCommand() {
        return command;
    }

    public String toSerializedString() {
        StringBuilder sb = new StringBuilder(command);
        if (!parameters.isEmpty()) {
            sb.append("?");
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                sb.append(param.getKey())
                    .append("=")
                    .append(param.getValue())
                    .append(";");
            }
            // remove trailing semicolon
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("Command: '%s' Parameters: %s", command, parameters.toString());
    }

}
