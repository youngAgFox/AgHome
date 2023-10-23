package com.ag.database;

public class EnumHelper {
    public static String toPropertyString(Enum<?> e) {
        return e.name().toLowerCase();
    }
}
