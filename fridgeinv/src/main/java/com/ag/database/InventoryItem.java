package com.ag.database;

import java.io.Serializable;

public class InventoryItem implements Storable, Serializable {

    private long id;

    public InventoryItem() {

    }

    public InventoryItem(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

}
