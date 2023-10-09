package com.ag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Synch {

    public static final String STORE_SURROGATE_KEY = "store_sk";

    private final File keyFile = new File(".keys");
    private Map<String, Long> surrogateKeys;

    private static Synch instance;

    @SuppressWarnings("unchecked")
    private Synch() {
        if (keyFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyFile))) {
                surrogateKeys = (Map<String, Long>) ois.readObject();
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException("Synch surrogate keys file is saved with unrecognized data", e);
            }
        } else {
            surrogateKeys = new HashMap<>();
        }
    }

    public static Synch getInstance() {
        if (null == instance) {
            instance = new Synch();
        }
        return instance;
    }

    public synchronized long nextKey(String keyName) {
        long surrogate = surrogateKeys.getOrDefault(keyName, 0L);
        surrogateKeys.put(keyName, surrogate + 1L);
        saveKeys();
        return surrogate;
    }

    private void saveKeys() {
        try (ObjectOutputStream oos  = new ObjectOutputStream(new FileOutputStream(keyFile, false))) {
            oos.writeObject(surrogateKeys);
        } catch (IOException e) {
            throw new RuntimeException("FATAL: Failed to properly persist surrogate keys.", e);
        }
    }

}
