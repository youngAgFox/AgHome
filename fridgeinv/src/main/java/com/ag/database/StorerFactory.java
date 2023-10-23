package com.ag.database;

import java.io.File;
import java.io.IOException;

public class StorerFactory {

    // storer directories
    private static final String STORER_DIR = ".fridgeinv" + File.separator + "storage" + File.separator;
    private static final String ITEM_STORER_PATH = STORER_DIR + "item_storer";
    private static final String ITEM_STORER_META_PATH = ITEM_STORER_PATH + "_meta";
    private static final String STORE_STORER_PATH = STORER_DIR + "store_storer";
    private static final String STORE_STORER_META_PATH = STORE_STORER_PATH + "_meta";


    private static FlatFileStorer<InventoryItem> itemStorer;
    private static FlatFileStorer<Store> storeStorer;

    public static Storer<InventoryItem> getItemStorer() {
        if (null == itemStorer) {
            itemStorer = new FlatFileStorer<>();
            openStorer(itemStorer, ITEM_STORER_META_PATH, ITEM_STORER_PATH);
        }
        return itemStorer;
    }

    public static Storer<Store> getStoreStorer() {
        if (null == storeStorer) {
            storeStorer = new FlatFileStorer<>();
            openStorer(storeStorer, STORE_STORER_META_PATH, STORE_STORER_PATH);
        }
        return storeStorer;
    }

    private static <T extends Storable> void openStorer(FlatFileStorer<T> storer, String metaDir, String dir) {
        try {
            storer.open(metaDir, dir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storer object. Meta dir: " + metaDir + "; Dir: " + dir, e);
        }
    }
}
