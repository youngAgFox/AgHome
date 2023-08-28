package com.ag;

import java.util.HashMap;

public class Synch {
    private static final HashMap<String, Long> surrogateKeys = new HashMap<>();

    private Synch() {}

    public static synchronized long nextKey(String keyName) {
        long surrogate = surrogateKeys.getOrDefault(keyName, 0L);
        surrogateKeys.put(keyName, surrogate + 1L);
        return surrogate;
    }

}
