package com.ag.database;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ag.TransmissionHelper;

public abstract class Storable implements Serializable, Transferable {

    private long id;
    private Map<String, Object> properties = new HashMap<>();

    public Storable(long id) {
        this.id = id;
    }

    public Storable() {}

    /**
     * Sets a property of the Storable. Can create new properties when the property name does
     * not yet exist. 
     * 
     * @param propertyName
     * @param value
     */
    public void set(String propertyName, Object value) {
        properties.put(propertyName, value);
    }

    public void setInteger(Map<String, String> map, String propertyName) {
        Integer value = Integer.parseInt(map.get(propertyName));
        properties.put(propertyName, value);
    }

    public Integer getInteger(String propertyName) {
        return (Integer) get(propertyName);
    }

    public void setBoolean(Map<String, String> map, String propertyName) {
        Boolean value = Boolean.parseBoolean(map.get(propertyName));
        properties.put(propertyName, value);
    }

    public Boolean getBoolean(String propertyName) {
        return (Boolean) get(propertyName);
    }

    public void setDouble(Map<String, String> map, String propertyName) {
        Double value = Double.parseDouble(map.get(propertyName));
        properties.put(propertyName, value);
    }

    public Double getDouble(String propertyName) {
        return (Double) get(propertyName);
    }

    public void setFloat(Map<String, String> map, String propertyName) {
        Float value = Float.parseFloat(map.get(propertyName));
        properties.put(propertyName, value);
    }

    public Float getFloat(String propertyName) {
        return (Float) get(propertyName);
    }

    public void setLong(Map<String, String> map, String propertyName) {
        Long value = Long.parseLong(map.get(propertyName));
        properties.put(propertyName, value);
    }

    public Long getLong(String propertyName) {
        return (Long) get(propertyName);
    }

    public void setString(Map<String, String> map, String propertyName) {
        properties.put(propertyName, map.get(propertyName));
    }

    public String getString(String propertyName) {
        return (String) properties.get(propertyName);
    }

    public void setDate(Map<String, String> map, String propertyName) {
        Date value = TransmissionHelper.parseDate(map.get(propertyName));
        properties.put(propertyName, value);
    }

    public Date getDate(String propertyName) {
        return (Date) get(propertyName);
    }

    /**
     * Gets a property from the Storable
     * 
     * @param propertyName
     * @return the property value
     */
    public Object get(String propertyName) {
        return properties.get(propertyName);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Returns a unique Id representing this stored object.
     * 
     * @return a unique Id.
     */
    public long getId() {
        return id;
    }

    @Override
    public Map<String, String> toArgs() {
        return ParameterHelper.createStringValueMap(properties);
    }

    @Override
    public String getParamString() {
        return ParameterHelper.getPropertyString(properties);
    }
}
