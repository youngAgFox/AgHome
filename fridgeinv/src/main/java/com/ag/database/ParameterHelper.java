package com.ag.database;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.ag.DynamicObject;

public class ParameterHelper {

    public static <S extends Storable> void validateUniqueParameter(String message, Storer<S> storer,
            Predicate<S> matches) {

        try {
            if (storer.contains(matches)) {
                throw new IllegalArgumentException(message);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to validate uniqueness (" + message + ")", e);
        }
    }

    public static void validateArgsAreNotNull(DynamicObject args, String... argNames) {
        Set<String> nullArgs = new HashSet<>();
        for (String argName : argNames) {
            if (!args.containsKey(argName) || null == args.get(argName)) {
                nullArgs.add(argName);
            }
        }
        if (!nullArgs.isEmpty()) {
            throw createInvalidArgsException("Args must be non-null: ", nullArgs);
        }
    }

    public static void validateArgsExist(DynamicObject args, String... argNames) {
        Set<String> missingArgs = new HashSet<>();
        for (String argName : argNames) {
            if (!args.containsKey(argName)) {
                missingArgs.add(argName);
            }
        }
        if (!missingArgs.isEmpty()) {
            throw createInvalidArgsException("Args must be valid keys: ", missingArgs);
        }
    }

    public static String getPropertyString(Map<String, Object> properties) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            sb.append("<")
                .append(entry.getValue().getClass().getSimpleName())
                .append(": ")
                .append(entry.getKey())
                .append("> ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }


    private static IllegalArgumentException createInvalidArgsException(String message, Set<String> missingReqParams) {
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
        return new IllegalArgumentException(sb.toString());
    }
}
