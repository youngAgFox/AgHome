package com.ag.database;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import com.ag.DynamicObject;
import com.ag.Server;
import com.ag.json.JsonConfig;

public class FlatFileStorerFactory {

    private static final Map<String, FlatFileStorer<?>> storers = new HashMap<>();

    public static <T extends Storable> FlatFileStorer<T> getStorer(Class<T> storedClass) {
        String key = JsonConfig.getClassConfigPropertyName(storedClass);
        return getStorer(storedClass, key);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Storable> FlatFileStorer<T> getStorer(Class<T> storedClass, String key) {
        FlatFileStorer<?> storer = storers.get(key);
        if (null == storer) {
            FlatFileStorer<T> newStorer = new FlatFileStorer<>();
            DynamicObject model = Server.config.getConfig(newStorer.getClass(), storedClass);
            String filename = (String) model.get("name");
            String dir = (String) model.get("directory");
            String path = Server.ROOT_DIRECTORY + dir + filename;
            String metaIdentifier = (String) model.get("metaIdentifier");
            String metaPath = Server.ROOT_DIRECTORY + dir + metaIdentifier + filename;
            openStorer(newStorer , metaPath, path);
            storers.put(key, newStorer);
            return newStorer;
        }
        return (FlatFileStorer<T>) storer;
    }

    private static <T extends Storable> void openStorer(FlatFileStorer<T> storer, String metaDir, String dir) {
        try {
            System.out.println("Opened storer @ " + dir);
            storer.open(metaDir, dir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storer object. Meta dir: '" + metaDir + "''; Dir: '" + dir + "'", e);
        }
    }
}
