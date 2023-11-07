package com.ag.database;

import com.ag.json.JsonClass;

@JsonClass
public class Store extends Storable {

    private String name;

    public Store() {}

    public Store(long id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " (" + getId() + ")";
    }
}
