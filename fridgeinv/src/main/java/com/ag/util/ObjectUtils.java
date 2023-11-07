package com.ag.util;

public class ObjectUtils {
    
    public static Object getNonNull(Object ... objects) {
        for (Object o : objects) {
            if (null != o) {
                return o;
            }
        }
        return null;
    }
}
