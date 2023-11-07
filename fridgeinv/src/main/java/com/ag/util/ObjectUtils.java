package com.ag.util;

public class ObjectUtils {
    
    public static Object getFirstNonNull(Object ... objects) {
        for (Object o : objects) {
            if (null != o) {
                return o;
            }
        }
        return null;
    }

    public static <T> T toWrapperType(Class<T> targetType, Object wrappedPrimitive) {
        if (targetType.isAssignableFrom(Integer.class)) {
            int toSet = 0;
            if (wrappedPrimitive instanceof Long) {
                Long longPrimitive = (Long) wrappedPrimitive;
                if (longPrimitive > Integer.MAX_VALUE || longPrimitive < Integer.MIN_VALUE) {
                    throw new IllegalArgumentException("Long value '" + longPrimitive + " is out of Integer range");
                }
                toSet = longPrimitive.intValue();
            } else if (wrappedPrimitive instanceof Short) {
                Short shortPrimitive = (Short) wrappedPrimitive;
                toSet = shortPrimitive.intValue();
            } else {
                toSet = (Integer) wrappedPrimitive;
            }
            return targetType.cast(toSet);
        } else if (targetType.isAssignableFrom(Short.class)) {
            short toSet = 0;
            if (wrappedPrimitive instanceof Long) {
                Long longPrimitive = (Long) wrappedPrimitive;
                if (longPrimitive > Short.MAX_VALUE || longPrimitive < Short.MIN_VALUE) {
                    throw new IllegalArgumentException("Long value '" + longPrimitive + " is out of Short range");
                }
                toSet = longPrimitive.shortValue();
            } else if (wrappedPrimitive instanceof Integer) {
                Integer intPrimitive = (Integer) wrappedPrimitive;
                if (intPrimitive > Short.MAX_VALUE || intPrimitive < Short.MIN_VALUE) {
                    throw new IllegalArgumentException("Integer value '" + intPrimitive + " is out of Short range");
                }
                toSet = intPrimitive.shortValue();
            } else {
                toSet = (Short) wrappedPrimitive;
            }
            return targetType.cast(toSet);
        } else if (targetType.isAssignableFrom(Long.class)) {
            long toSet = 0;
            if (wrappedPrimitive instanceof Short) {
                Short shortPrimitive = (Short) wrappedPrimitive;
                toSet = shortPrimitive.longValue();
            } else if (wrappedPrimitive instanceof Integer) {
                Integer intPrimitive = (Integer) wrappedPrimitive;
                toSet = intPrimitive.longValue();
            } else {
                toSet = (Long) wrappedPrimitive;
            }
            return targetType.cast(toSet);
        } else if (targetType.isAssignableFrom(Float.class)) {
            float toSet = 0.0f;
            if (wrappedPrimitive instanceof Double) {
                Double doublePrimitive = (Double) wrappedPrimitive;
                if (doublePrimitive < Float.MIN_VALUE || doublePrimitive > Float.MAX_VALUE) {
                    throw new IllegalArgumentException("Double value '" + doublePrimitive + " is out of Float range");
                }
                toSet = doublePrimitive.floatValue();
            } else {
                toSet = (Float) wrappedPrimitive;
            }
            return targetType.cast(toSet);
        } else if (targetType.isAssignableFrom(Double.class)) {
            double toSet = 0.0;
            if (wrappedPrimitive instanceof Float) {
                Float floatPrimitive = (Float) wrappedPrimitive;
                toSet = floatPrimitive.doubleValue();
            } else {
                toSet = (Double) wrappedPrimitive;
            }
            return targetType.cast(toSet);
        } else if (targetType.isAssignableFrom(Boolean.class)
                || targetType.isAssignableFrom(Byte.class)
                || targetType.isAssignableFrom(Character.class)) {
            return targetType.cast(wrappedPrimitive);
        }

        throw new RuntimeException("Component type '" + targetType.getName() + "' was not a primitive");
    }
}
