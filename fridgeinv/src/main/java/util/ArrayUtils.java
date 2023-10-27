package util;

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
    
}