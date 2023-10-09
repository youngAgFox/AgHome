package com.ag.database;

import java.io.Serializable;

public class Store implements Storable, Serializable {

    private long id;
    private String name;

    public Store() {
    }

    public Store(long id, String name) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return id;
    }

}
