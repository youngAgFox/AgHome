package com.ag;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public class TransmissionHelper {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static Date parseDate(String dateString) {
        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse '" + dateString + "'", e);
        }

    }

    public static String formatDate(Date date) {
        return formatter.format(date);
    }

    public static String toTransmissionString(Object obj) {
        String value = null;
            // primitives and basic interfaces
            if (obj instanceof String) {
                value = (String) obj;
            } else if (obj instanceof Boolean) {
                value = String.valueOf((Boolean) obj);
            } else if (obj instanceof Integer) {
                value = String.valueOf((Integer) obj);
            } else if (obj instanceof Long) {
                value = String.valueOf((Long) obj);
            } else if (obj instanceof Double) {
                value = String.valueOf((Double) obj);
            } else if (obj instanceof Float) {
                value = String.valueOf((Float) obj);
            } else if (obj instanceof Object[]) {
                value = toTransmissionArrayString((Object[]) obj);
            } else if (obj instanceof Collection) {
                value = toTransmissionListString((Collection<?>) obj);
            } else if (obj instanceof Map) {
                value = toTransmissionMapString((Map<?,?>) obj);
            } else if (obj instanceof Date) {
                // special date handling
                value = formatDate((Date) obj);
            }
            
            // default to the objects toString()
            if (null != obj && null == value) {
                value = obj.toString();
            }

            return value;
    }

   private static String toTransmissionMapString(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        for (Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            // both the key and value are quoted separately, and the overall key value pair is quoted with separate quote char
            sb.append("\"'")
                .append(toTransmissionString(key))
                .append("'")
                .append(":")
                .append("'")
                .append(toTransmissionString(value))
                .append("'\",");
        }
        // remove trailing comma
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

 private static String toTransmissionArrayString(Object[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (Object ele : arr) {
            sb.append("'")
                    .append(toTransmissionString(ele))
                    .append("'")
                    .append(",");
        }
        // remove trailing comma
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }

    private static String toTransmissionListString(Iterable<?> it) {
        StringBuilder sb = new StringBuilder("[");
        for (Object ele : it) {
            sb.append("'")
                    .append(toTransmissionString(ele))
                    .append("'")
                    .append(",");
        }
        // remove trailing comma
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }
}
