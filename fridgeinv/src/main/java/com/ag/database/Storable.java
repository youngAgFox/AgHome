package com.ag.database;

import java.io.Serializable;

public class Storable implements Serializable {

    private long id;

    public Storable(long id) {
        this.id = id;
    }

    public Storable() {}

    /**
     * Returns a unique Id representing this stored object.
     * 
     * @return a unique Id.
     */
    public long getId() {
        return id;
    }
}
