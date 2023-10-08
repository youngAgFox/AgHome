package com.ag.database;

import java.io.Serializable;

public class Store implements Storable, Serializable {

    private long id;

    public Store() {
    }

    public Store(int id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

}
