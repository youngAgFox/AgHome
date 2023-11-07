package com.ag.util;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectionUtils {

    public static <T> Collection<T> select(Collection<T> list, Predicate<T> select) {
        return list.stream()
            .filter(select)
            .collect(Collectors.toList());
    }

    public static <T> boolean isEmptyOrNull(Collection<T> collection) {
        return null == collection || collection.isEmpty();
    }

    public static <T> boolean isNotEmptyOrNull(Collection<T> collection) {
        return null != collection && !collection.isEmpty();
    }

}
