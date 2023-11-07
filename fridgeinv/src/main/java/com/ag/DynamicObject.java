package com.ag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ag.json.JsonFormat;
import com.ag.json.JsonFormatter;
import com.ag.util.ObjectUtils;

/**
 * Represents an object that can be considered either a value, array, or object.
 * Type is relaxed to runtime errors in order to accomadate formats such as JSON
 * and the use of arrays of multiple types (such as in javascript).
 * <p>
 * Operations are valid depending on the type of the DynamicObject. For example, the
 * {@link #add(Object)} would be used for arrays, {@link #put(String, Object)} for objects, 
 * while value types are limited to {@link #set(Object)}. Using methods on the wrong
 * type will throw {@link InvalidDynamicTypeOperationException}.
 */
public class DynamicObject implements Iterable<DynamicObject> {

    private final DynamicType type;
    private Object value;
    private Map<String, DynamicObject> properties;
    private List<DynamicObject> elements;
    
    public DynamicObject(DynamicType type) {
        this(type, null);
    }

    public DynamicObject(DynamicType type, Object value) {
        this.type = type;
        if (DynamicType.ARRAY == type) {
            elements = new ArrayList<>();
        } else if (DynamicType.OBJECT == type) {
            properties = new HashMap<>();
        }
        this.value = value;
    }

    public Object get() {
        validateType(DynamicType.VALUE);
        return value;
    }

    public Object get(int index) {
        DynamicObject entity = getDynamicObject(index);
        return getValue(entity);
    }

    public Object get(String key) {
        DynamicObject entity = getDynamicObject(key);
        return getValue(entity);
    }

    public Object getOrDefault(String key, Object value) {
        if (null == properties || !properties.containsKey(key)) {
            return value;
        }
        DynamicObject entity = getDynamicObject(key);
        return ObjectUtils.getNonNull(getValue(entity), value);
    }

    public DynamicObject set(Object value) {
        validateType(DynamicType.VALUE);
        this.value = value;
        return this;
    }

    public DynamicObject set(int index, Object value) {
        validateType(DynamicType.ARRAY);
        elements.set(index, castOrWrapToEntity(value));
        return this;
    }

    public DynamicObject add(Object value) {
        validateType(DynamicType.ARRAY);
        elements.add(castOrWrapToEntity(value));
        return this;
    }

    public DynamicObject put(String key, Object value) {
        validateType(DynamicType.OBJECT);
        properties.put(key, castOrWrapToEntity(value));
        return this;
    }

    private DynamicObject castOrWrapToEntity(Object value) {
        DynamicObject entity;
        if (value instanceof DynamicObject) {
            entity = (DynamicObject) value;
        } else {
            entity = new DynamicObject(DynamicType.VALUE, value);
        }
        return entity;
    }

    public DynamicObject putArray(String key, DynamicObject array) {
        validateType(DynamicType.OBJECT);
        array.validateType(DynamicType.ARRAY);
        properties.put(key, array);
        return array;
    }

    public DynamicObject putArray(String key) {
        return putArray(key, new DynamicObject(DynamicType.ARRAY));
    }

    public DynamicObject addArray(DynamicObject array) {
        validateType(DynamicType.ARRAY);
        array.validateType(DynamicType.ARRAY);
        elements.add(array);
        return array;
    }

    public DynamicObject addArray() {
        return addArray(new DynamicObject(DynamicType.ARRAY));
    }

    public DynamicObject putObject(String key, DynamicObject object) {
        validateType(DynamicType.OBJECT);
        object.validateType(DynamicType.OBJECT);
        properties.put(key, object);
        return object;
    }

    public DynamicObject putObject(String key) {
        return putObject(key, new DynamicObject(DynamicType.OBJECT));
    }

    public DynamicObject addObject(DynamicObject object) {
        validateType(DynamicType.ARRAY);
        object.validateType(DynamicType.OBJECT);
        elements.add(object);
        return object;
    }

    public DynamicObject addObject() {
        return addObject(new DynamicObject(DynamicType.OBJECT));
    }

    private Object getValue(DynamicObject entity) {
        switch (entity.type) {
            case ARRAY:
                return entity.elements;
            case OBJECT:
                return entity.properties;
            case VALUE:
                return entity.get();
            default:
                throw new RuntimeException("No handled get() for " + entity.type + " type");
        }
    }

    public DynamicObject getDynamicObject(int index) {
        validateType(DynamicType.ARRAY);
        DynamicObject entity = elements.get(index);
        if (null == entity) {
            throw new NoSuchFieldError("No entity at index " + index);
        }
        return entity;
    }

    public DynamicObject getDynamicObject(String key) {
        validateType(DynamicType.OBJECT);
        DynamicObject entity = properties.get(key);
        if (null == entity) {
            throw new NoSuchFieldError("No entity at key '" + key + "'");
        }
        return entity;
    }

    public boolean containsKey(String key) {
        validateType(DynamicType.OBJECT);
        return properties.containsKey(key);
    }

    public DynamicType getType() {
        return type;
    }

    public int size() {
        if (DynamicType.OBJECT == type) {
            return properties.size();
        } else if (DynamicType.ARRAY == type) {
            return elements.size();
        }
        throw new InvalidDynamicTypeOperationException("Cannot call size() on " + type + " type.");
    }

    private void validateType(DynamicType type) {
        if (this.type != type) {
            throw new InvalidDynamicTypeOperationException("Operation invalid for type " + this.type + ". Expected type " + type);
        }
    }

    public Set<Map.Entry<String, DynamicObject>> getProperties() {
        validateType(DynamicType.OBJECT);
        return properties.entrySet();
    }

    @JsonFormat
    private String format(JsonFormatter formatter, int indent) {
        switch (type) {
            case ARRAY:
                return formatter.formatObject(elements, indent);
            case OBJECT:
                return formatter.formatObject(properties, indent);
            case VALUE:
                return formatter.formatObject(value, indent);
            default:
                throw new RuntimeException("No format configuration for DO '" + type + "' type");
        }
    }

    @Override
    public Iterator<DynamicObject> iterator() {
        switch (type) {
            case ARRAY:
                return elements.iterator();
            case OBJECT:
                return properties.values().iterator();
            case VALUE:
            default:
                throw new InvalidDynamicTypeOperationException("No iterator() for " + type + " type");
        }
    }

    @Override
    public String toString() {
        switch (type) {
            case ARRAY:
                return elements.toString();
            case OBJECT:
                return properties.toString();
            case VALUE:
                return null == value ? "null" : value.toString();
            default:
                return super.toString();
        }
    }
}
