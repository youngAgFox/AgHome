package com.ag.database;

public class InventoryItem implements Storeable {

    private final long id;

    public InventoryItem(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

}
