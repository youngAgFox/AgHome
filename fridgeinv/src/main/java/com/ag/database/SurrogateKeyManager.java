package com.ag.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.ag.Network.Server;

public class SurrogateKeyManager {

    private static final long SURROGATE_KEY_FIRST_KEY = 1;

    private final File keyFile;
    private Map<String, Long> surrogateKeys;

    private static SurrogateKeyManager instance;

    @SuppressWarnings("unchecked")
    private SurrogateKeyManager() {
        keyFile = new File(Server.ROOT_DIRECTORY + (String) Server.config.getConfig(SurrogateKeyManager.class, "path"));
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

    public static SurrogateKeyManager getInstance() {
        if (null == instance) {
            instance = new SurrogateKeyManager();
        }
        return instance;
    }

    public synchronized long nextKey(Class<?> cls) {
        long surrogate = surrogateKeys.getOrDefault(cls.getSimpleName(), SURROGATE_KEY_FIRST_KEY);
        surrogateKeys.put(cls.getSimpleName(), surrogate + 1);
        saveKeys();
        return surrogate;
    }

    private void saveKeys() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(keyFile, false))) {
            oos.writeObject(surrogateKeys);
        } catch (IOException e) {
            throw new RuntimeException("FATAL: Failed to properly persist surrogate keys", e);
        }
    }

}
