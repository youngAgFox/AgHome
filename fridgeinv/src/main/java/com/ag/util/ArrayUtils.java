package com.ag.util;

import java.util.function.Predicate;

public class ArrayUtils {

    public static <T> T find(T[] arr, Predicate<T> matches) {
        for (T ele : arr) {
            if (matches.test(ele)) {
                return ele;
            }
        }
        return null;
    }

    public static <T> T findOrDefault(T[] arr, Predicate<T> matches, T dflt) {
        T ele = find(arr, matches);
        return null != ele ? ele : dflt;
    }

    public static Object[] toObjectArray(Object obj) {
        if (!obj.getClass().isArray()) {
            throw new RuntimeException("Class " + obj.getClass().getName() + " is not an array type.");
        }

        Class<?> arrayType = obj.getClass().getComponentType();

        if (arrayType.isAssignableFrom(boolean.class)) {
            return toWrapperArray((boolean[]) obj);
        } else if (arrayType.isAssignableFrom(int.class)) {
            return toWrapperArray((int[]) obj);
        } else if (arrayType.isAssignableFrom(long.class)) {
            return toWrapperArray((long[]) obj);
        } else if (arrayType.isAssignableFrom(short.class)) {
            return toWrapperArray((short[]) obj);
        } else if (arrayType.isAssignableFrom(double.class)) {
            return toWrapperArray((double[]) obj);
        } else if (arrayType.isAssignableFrom(float.class)) {
            return toWrapperArray((float[]) obj);
        } else if (arrayType.isAssignableFrom(byte.class)) { 
            return toWrapperArray((byte[]) obj);
        } else if (arrayType.isAssignableFrom(char.class)) {
            return toWrapperArray((char[]) obj);
        }

        return (Object[]) obj;
    }

    private static Object[] toWrapperArray(boolean[] bools) {
        Boolean[] wrappers = new Boolean[bools.length];
        for (int i = 0; i < bools.length; i++) {
            wrappers[i] = bools[i];
        }
        return wrappers;
    }

    private static Object[] toWrapperArray(short[] shorts) {
        Short[] wrappers = new Short[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            wrappers[i] = shorts[i];
        }
        return wrappers;
    }

    private static Object[] toWrapperArray(int[] ints) {
        Integer[] wrappers = new Integer[ints.length];
        for (int i = 0; i < ints.length; i++) {
            wrappers[i] = ints[i];
        }
        return wrappers;
    }

    private static Object[] toWrapperArray(long[] longs) {
        Long[] wrappers = new Long[longs.length];
        for (int i = 0; i < longs.length; i++) {
            wrappers[i] = longs[i];
        }
        return wrappers;
    }

    private static Object[] toWrapperArray(double[] doubles) {
        Double[] wrappers = new Double[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            wrappers[i] = doubles[i];
        }
        return wrappers;
    }

    private static Object[] toWrapperArray(float[] floats) {
        Float[] wrappers = new Float[floats.length];
        for (int i = 0; i < floats.length; i++) {
            wrappers[i] = floats[i];
        }
        return wrappers;
    }

    private static Object[] toWrapperArray(byte[] bytes) {
        Byte[] wrappers = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            wrappers[i] = bytes[i];
        }
        return wrappers;
    }

    private static Object[] toWrapperArray(char[] chars) {
        Character[] wrappers = new Character[chars.length];
        for (int i = 0; i < chars.length; i++) {
            wrappers[i] = chars[i];
        }
        return wrappers;
    }

}