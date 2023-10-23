package com.ag.database;

import java.util.Date;
import java.util.Map;

public class InventoryItem extends Storable {

    private enum Property {
        NAME, QUANTITY, LAST_ADDED;

        public String getKey() {
            return EnumHelper.toPropertyString(this);
        }
    }

    // requried for Serializable
    public InventoryItem() {}

    public InventoryItem(long id) {
        super(id);
    }

    public String getName() {
        return getString(Property.NAME.getKey());
    }

    public void setName(String name) {
        set(Property.NAME.getKey(), name);
    }

    public int getQuantity() {
        return getInteger(Property.QUANTITY.getKey());
    }

    public void setQuantity(int quantity) {
        set(Property.QUANTITY.getKey(), quantity);
    }

    public Date getLastAdded() {
        return getDate(Property.LAST_ADDED.getKey());
    }

    public void setLastAdded(Date lastAdded) {
        set(Property.LAST_ADDED.getKey(), lastAdded);
    }

    @Override
    public void initialize(Map<String, String> params) {
        ParameterHelper.validateParametersAreNotNull(params, "name", "quantity", "lastAdded");

        setString(params, Property.NAME.getKey());
        setInteger(params, Property.QUANTITY.getKey());
        setDate(params, Property.LAST_ADDED.getKey());

        Storer<InventoryItem> itemStorer = StorerFactory.getItemStorer();
        final String name = getName();
        ParameterHelper.validateUniqueParameter(Property.NAME.getKey(), itemStorer, is -> is.getName().equals(name));
    }

}
