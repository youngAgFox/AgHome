package com.ag.json;

import java.util.Map.Entry;

import com.ag.DynamicObject;

/**
 * Represents a Json configuration file. Saves configurations at a class level. Generic classes
 * can supply additional type classes for added specificity. The most specific configuration wins.
 */
public class JsonConfig {

    private DynamicObject root;
    
    public JsonConfig(String path) {
        JsonParser parser = new JsonParser();
        root = parser.parse(JsonConfig.class.getResourceAsStream(path));
    }

    public static String getClassConfigPropertyName(Class<?> target, Class<?>... types) {
        if (null == target) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(target.getSimpleName()).append(".");
        for (Class<?> type : types) {
            sb.append(type.getSimpleName()).append(".");
        }
        // exclude the trailing '.'
        return sb.substring(0, sb.length() - 1);
    }

    public DynamicObject getConfig(Class<?> target, Class<?>... types) {
        Class<?> currentClass = target;
        String key = getClassConfigPropertyName(currentClass, types);
        DynamicObject configs = getConfig(key);
        if (types.length > 0) {
            // also get the generic no type configs if they exist, and add them without replacement
            try {
                DynamicObject noTypeConfigs = getConfig(getClassConfigPropertyName(target));
                addMissingFields(noTypeConfigs, configs);
            } catch (NoSuchFieldError ignored) {
                // don't exist, non-fatal
            }
        }
        return configs;
    }

    private void addMissingFields(DynamicObject source, DynamicObject destination) {
        for (Entry<String, DynamicObject> field : source.getProperties().entrySet()) {
            if (!destination.containsKey(field.getKey())) {
                destination.put(field.getKey(), field.getValue());
            }
        }
    }

    public DynamicObject getRoot() {
        return root;
    }

    public DynamicObject getConfig(String name) {
        return root.getDynamicObject(name);
    }

    public DynamicObject getConfig(int index) {
        return root.getDynamicObject(index);
    }

    public Object get(String string) {
        return root.get(string);
    }

    public Object get(int index) {
        return root.get(index);
    }

    public Object getConfig(Class<?> target, String propertyName) {
        return getConfig(target).get(propertyName);
    }

}
