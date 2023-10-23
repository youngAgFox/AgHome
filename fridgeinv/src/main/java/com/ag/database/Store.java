package com.ag.database;

import java.util.Map;

public class Store extends Storable {

    private enum Property {
        NAME;

        public String getKey() {
            return EnumHelper.toPropertyString(this);
        }
    }

    public Store() {}

    public Store(long id) {
        super(id);
    }

    public String getName() {
        return getString(Property.NAME.getKey());
    }

    public void setName(String name) {
        set(Property.NAME.getKey(), name);
    }

    @Override
    public void initialize(Map<String, String> params) {
        ParameterHelper.validateParametersAreNotNull(params, Property.NAME.getKey());

        setString(params, Property.NAME.getKey());

        Storer<Store> storeStorer = StorerFactory.getStoreStorer();
        final String storeName = getName();
        ParameterHelper.validateUniqueParameter("Store name '" + storeName + "' already exists (check is case insensitive).", 
            storeStorer, store -> store.getName().equalsIgnoreCase(storeName));
    }
}
