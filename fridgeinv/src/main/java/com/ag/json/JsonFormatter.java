package com.ag.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ag.DynamicObject;
import com.ag.util.ArrayUtils;
import com.ag.util.DateUtils;
import com.ag.util.StringUtils;

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

    private String formatFromCustomFormat(Method customFormat, Object object, int indent) {
        try {
            customFormat.setAccessible(true);
            Class<?>[] params = customFormat.getParameterTypes();
            if (1 == params.length) {
                if (params[0].isAssignableFrom(JsonFormatter.class)) {
                    return (String) customFormat.invoke(object, this);
                } else if (params[0].isPrimitive() && params[0] == int.class) {
                    return (String) customFormat.invoke(object, indent);
                } else {
                    throw new RuntimeException("Failed to format using custom formatter method. Unexpected parameter type.");
                }
            } else if (2 == params.length) {
                if (params[0].isAssignableFrom(JsonFormatter.class)) {
                    return (String) customFormat.invoke(object, this, indent);
                } else if (params[0].isPrimitive() && params[0] == int.class) {
                    return (String) customFormat.invoke(object, indent, this);
                } else {
                    throw new RuntimeException("Failed to format using custom formatter method. Unexpected parameter type.");
                }
            }
            return (String) customFormat.invoke(object);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Failed to format using custom formatter method", e);
        }
    }

    public String format(Object object) {
        return formatObject(object, 0);
    }

    public String formatObject(Object obj, int indent) {
        String value = null;

        if (null == obj) {
            return "null";
        }

        // check for custom format method
        Method customFormat = getCustomFormatMethod(obj);
        if (null != customFormat) {
            return formatFromCustomFormat(customFormat, obj, indent);
        }

        // primitives and basic interfaces
        if (obj instanceof String) {
            value = StringUtils.quote((String) obj);
        } else if (obj instanceof Boolean) {
            value = String.valueOf((boolean) obj);
        } else if (obj instanceof Integer) {
            value = String.valueOf((int) obj);
        } else if (obj instanceof Long) {
            value = String.valueOf((long) obj);
        } else if (obj instanceof Double) {
            value = String.valueOf((double) obj);
        } else if (obj instanceof Float) {
            value = String.valueOf((float) obj);
        } else if (obj instanceof Byte) { 
            value = String.valueOf((byte) obj);
        } else if (obj instanceof Character) {
            value = String.valueOf((char) obj);
        } else if (obj.getClass().isArray()) {
            Object[] objArray = ArrayUtils.toObjectArray(obj);
            value = formatArray(objArray, indent);
        } else if (obj instanceof Collection) {
            value = formatCollection((Collection<?>) obj, indent);
        } else if (obj instanceof Map) {
            value = formatMap((Map<?, ?>) obj, indent);
        } else if (obj instanceof Date) {
            // special date handling
            value = DateUtils.formatDate((Date) obj);
        }

        // print off the objects properties
        if (null != obj && null == value) {
            value = formatObjectFields(obj, indent);
        }

        return value;
    }

    private String formatObjectFields(Object obj, int indent) {
        Field[] fields = obj.getClass().getDeclaredFields();
        Map<String, Object> map = new HashMap<>(fields.length * 2);
        for (Field field : fields) {
            String fieldName = JsonHelper.getJsonFieldName(obj.getClass(), field);
            if (null == fieldName) {
                continue;
            }
            try {
                field.setAccessible(true);
                map.put(fieldName, field.get(obj));
                field.setAccessible(false);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("Failed to format object " + obj.toString()
                        + "(" + obj.getClass().getName() + "): " + e.getMessage());
            }
        }
        return formatMap(map, indent);
    }

    private Method getCustomFormatMethod(Object object) {
        Method[] methods = object.getClass().getDeclaredMethods();
        Method customFormat = null;
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotationsByType(JsonFormat.class);
            if (annotations.length > 0) {
                if (null == customFormat) {
                    customFormat = method;
                } else {
                    throw new RuntimeException("There can only be one declared " + JsonFormat.class.getName()
                            + " annotation in the " + object.getClass().getName() + " class.");
                }
            }
        }

        return customFormat;
    }

    public void formatToStream(Object object, OutputStream out) throws IOException {
        String jsonString = format(object);
        out.write(jsonString.getBytes());
    }

    private String formatMap(Map<?, ?> map, int indent) {
        String start = isCompactFormat ? "{" : "\n" + " ".repeat(indent) + "{\n" + " ".repeat(indent);
        String assignFormat = isCompactFormat ? "" : " ";
        StringBuilder sb = new StringBuilder(start);
        int lastNewline = 0;
        for (Entry<?, ?> entry : map.entrySet()) {
            sb.append(formatObject(entry.getKey(), indent))
                    .append(":")
                    .append(assignFormat)
                    .append(formatObject(entry.getValue(), indent))
                    .append(",");
            if (!isCompactFormat && (sb.length() - lastNewline) >= 80) {
                sb.append('\n');
                lastNewline = sb.length();
            }
        }
        // remove trailing comma
        if (sb.length() > start.length()) {
            sb.setLength(sb.length() - 1);
        }
        String end = isCompactFormat ? "}" : "}\n";
        sb.append(end);
        return sb.toString();
    }

    private String formatArray(Object[] arr, int indent) {
        StringBuilder sb = new StringBuilder("[");
        for (Object ele : arr) {
            sb.append(formatObject(ele, indent))
                    .append(",");
        }
        // remove trailing comma
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }

    private String formatCollection(Iterable<?> it, int indent) {
        StringBuilder sb = new StringBuilder("[");
        for (Object ele : it) {
            sb.append(formatObject(ele, indent))
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
