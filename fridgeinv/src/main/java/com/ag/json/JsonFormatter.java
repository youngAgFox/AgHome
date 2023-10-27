package com.ag.json;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.ag.DynamicObject;

import util.DateUtils;

/**
 * Writes formatted valid Json Strings from {@link DynamicObject} models or other objects.
 */
public class JsonFormatter {
    
    private boolean isCompactFormat = true;

    public JsonFormatter() {}

    public JsonFormatter(boolean isCompactFormat) {
        this.isCompactFormat = isCompactFormat;
    }

    public boolean isCompactFormat() {
        return isCompactFormat;
    }

    public void setCompactFormat(boolean isCompactFormat) {
        this.isCompactFormat = isCompactFormat;
    }

    public String format(Object object) {
        // FIXME
        return "";
    }

    public void formatToStream(Object object, OutputStream out) {
        // FIXME
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
                value = DateUtils.formatDate((Date) obj);
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
