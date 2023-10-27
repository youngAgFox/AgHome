package com.ag.database;

import java.util.Date;

import com.ag.DynamicObject;
import com.ag.json.AutoInitAll;

@AutoInitAll
public class InventoryItem extends Storable {

    private String name;
    private int quantity;
    private Date lastAdded;

    // requried for Serializable
    public InventoryItem() {}

    public InventoryItem(long id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getLastAdded() {
        return lastAdded;
    }

    public void setLastAdded(Date lastAdded) {
        this.lastAdded = lastAdded;
    }

}
