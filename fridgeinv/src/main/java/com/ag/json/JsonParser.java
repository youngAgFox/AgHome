package com.ag.json;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ag.DynamicObject;
import com.ag.DynamicType;
import com.ag.util.ArrayUtils;
import com.ag.util.DateUtils;
import com.ag.util.StringUtils;

/**
 * Handles creating and initializing models of Json using the DynamicObject class or custom user instances.
 * For creating json text from a model, see  {@link JsonFormatter}.
 */
public class JsonParser {

    private Map<Class<?>, JsonInitializerConvertor> convertors = new HashMap<>();

    public JsonParser() {
        // automatically register Date ISO string conversion
        convertors.put(Date.class, str -> DateUtils.parseDate(str));
    }

    public interface JsonInitializerConvertor {
        Object convertField(String jsonString);
    }

    public Map<Class<?>, JsonInitializerConvertor> getConvertors() {
        return convertors;
    }

    public <T> T initialize(Class<T> cls, String jsonString) {
        if (null == jsonString) {
            throw new NullPointerException("jsonString must be non-null");
        }
        JsonTokenReader reader = new JsonTokenReader(new StringReader(jsonString));
        return initialize(cls, reader);
    }

    public <T> T initialize(Class<T> cls, InputStream inputStream) {
        if (null == inputStream) {
            throw new NullPointerException("inputStream must be non-null and support mark()");
        }
        JsonTokenReader reader = new JsonTokenReader(new BufferedReader(new InputStreamReader(inputStream)));
        return initialize(cls, reader);
    }

    // FIXME document that private classes must be static
    private <T> T initialize(Class<T> cls, JsonTokenReader reader) {
        try {
            Object object = null;
            DynamicObject model = parseJson(reader);
            if (!cls.isArray()) {
                object = cls.getConstructor().newInstance();
                initializeInstance(object, model);
            } else {
                object = createArrayOrList(cls, model);
            }
            return cls.cast(object);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new JsonParseException(e.getMessage());
        }
    }

    /**
     * Initialize all fields of the same name as the model, regardless of access modifier. Primitive (and Wrapper), 
     * DynamicObject, Date (String with 'yyyy-mm-ddThh:mm:ss' format), and String types are supported out of the box.
     * 
     * To create your own conversion implementation to convert String objects to something else, use the {@link #getConvertors()}
     * method and register your own objects. Note that the Date ISO string convertor is included in the convertors map, 
     * and can safely be removed or replaced.
     * 
     * @param cls
     * @param model
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public void initializeInstance(Object object, DynamicObject model) 
            throws IllegalArgumentException, IllegalAccessException, InstantiationException, 
                InvocationTargetException, NoSuchMethodException, SecurityException {

        initializeObject(object, model);
    }

    private void initializeObject(Object object, DynamicObject model) 
            throws IllegalArgumentException, IllegalAccessException, InstantiationException, 
                InvocationTargetException, NoSuchMethodException, SecurityException {

        if (!DynamicType.OBJECT.equals(model.getType())) {
            throw new JsonParseException("Unexpected model type for initializing object: " + model.getType());
        }

        Field[] fields = object.getClass().getDeclaredFields();
        boolean onlyInitAnnotatedFields = null == ArrayUtils.find(object.getClass().getAnnotations(), 
            ele -> ele.annotationType().isAssignableFrom(AutoInitAll.class));

        for (Field field : fields) {
            String name = getInitAnnotationFieldName(field);
            if (null == name) {
                // there is no annotation
                if (onlyInitAnnotatedFields) {
                    continue;
                }
                name = field.getName();
            }
            field.setAccessible(true);
            if (model.containsKey(name)) {
                DynamicObject fieldEntity = model.getDynamicObject(name);
                if (DynamicType.ARRAY == fieldEntity.getType()) {
                    field.set(object, createArrayOrList(field.getType(), fieldEntity));
                } else if (DynamicType.OBJECT == fieldEntity.getType()) {
                    Object fieldObject = field.getClass().getConstructor().newInstance();
                    initializeInstance(fieldObject, fieldEntity);
                    field.set(object, fieldObject);
                } else {
                    Object fieldValue = fieldEntity.get();
                    if (fieldValue.getClass().isAssignableFrom(field.getType())) {
                        field.set(object, fieldValue);
                    } else if (field.getType().isPrimitive()) {
                        setPrimitive(field, object, fieldValue);
                    } else if (fieldValue.getClass().isAssignableFrom(String.class)) {
                        for (Class<?> convertCls : convertors.keySet()) {
                            if (field.getType().isAssignableFrom(convertCls)) {
                                field.set(object, convertors.get(convertCls).convertField((String) fieldValue));
                                return;
                            }
                        }
                        throw new JsonInitializeException("No convertor registered that can convert String to '" + field.getType().getTypeName() + "'");
                    }
                }
            }
            field.setAccessible(false);
        }
    }

    private Object createArrayOrList(Class<?> arrayClass, DynamicObject arrayModel) {

        if (arrayClass.isArray()) {
            int len = arrayModel.size();
            Class<?> componentType = arrayClass.getComponentType();
            Object arr = Array.newInstance(componentType, len);
            for (int i = 0; i < len; i++) {
                try {
                    if (componentType.isPrimitive()) {
                        setPrimitiveArray(componentType, arr, i, arrayModel.get(i));
                    } else {
                        System.out.println(arrayModel.get(i).getClass().getName());
                        Array.set(arr, i, arrayModel.get(i));
                    }

                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(
                            "Failed to set index (" + i + ") to type " + arrayModel.get(i).getClass().getName()
                                    + " in " + componentType.getName() + " array",
                            e);
                }
            }
            return arr;
        } else if (arrayClass.isAssignableFrom(Collection.class)) {
            List<Object> arrList = new ArrayList<>();
            for (DynamicObject dynamicObject : arrayModel) {
                arrList.add(dynamicObject.get());
            }
            return arrList;
        } else {
            throw new JsonInitializeException(
                    "Failed to initialize array " + arrayClass.getName());
        }
    }

    /**
     * Gets the {@code @AutoInit} annotation associated with the field, and returns
     * the
     * fieldName it was set with. When set to the default (null or empty string),
     * the field's
     * name is returned instead. When the annotation does not exist, null is
     * returned.
     * 
     * @param field
     * @return the String field name to use to init this field per the annotation,
     *         or null when
     *         there is no annotation.
     */
    private String getInitAnnotationFieldName(Field field) {
        Annotation[] annotations = field.getAnnotations();
        Annotation annotation = ArrayUtils.find(annotations,
                ele -> ele.annotationType().isAssignableFrom(AutoInit.class));
        if (null == annotation) {
            return null;
        }
        AutoInit ai = (AutoInit) annotation;
        String key = ai.getFieldName();
        if (StringUtils.isEmptyOrNull(key)) {
            key = field.getName();
        }
        return key;
    }

    private void setPrimitive(Field field, Object object, Object wrappedPrimitive)
            throws IllegalArgumentException, IllegalAccessException {
        if (field.getType() == int.class) {
            field.setInt(object, (Integer) wrappedPrimitive);
        } else if (field.getType() == short.class) {
            field.setShort(object, (Short) wrappedPrimitive);
        } else if (field.getType() == long.class) {
            field.setLong(object, (Long) wrappedPrimitive);
        } else if (field.getType() == float.class) {
            field.setFloat(object, (Float) wrappedPrimitive);
        } else if (field.getType() == double.class) {
            field.setDouble(object, (Double) wrappedPrimitive);
        } else if (field.getType() == boolean.class) {
            field.setBoolean(object, (Boolean) wrappedPrimitive);
        } else if (field.getType() == byte.class) {
            field.setByte(object, (Byte) wrappedPrimitive);
        } else if (field.getType() == char.class) {
            field.setChar(object, (Character) wrappedPrimitive);
        }
    }

    private void setPrimitiveArray(Class<?> componentType, Object object, int index, Object wrappedPrimitive) {
        if (componentType.isAssignableFrom(int.class)) {
            Array.setInt(object, index, toWrapperType(Integer.class, wrappedPrimitive));
        } else if (componentType.isAssignableFrom(short.class)) {
            Array.setShort(object, index, toWrapperType(Short.class, wrappedPrimitive));
        } else if (componentType.isAssignableFrom(long.class)) {
            Array.setLong(object, index, toWrapperType(Long.class, wrappedPrimitive));
        } else if (componentType.isAssignableFrom(float.class)) {
            Array.setFloat(object, index, toWrapperType(Float.class, wrappedPrimitive));
        } else if (componentType.isAssignableFrom(double.class)) {
            Array.setDouble(object, index, toWrapperType(Double.class, wrappedPrimitive));
        } else if (componentType.isAssignableFrom(boolean.class)) {
            Array.setBoolean(object, index, toWrapperType(Boolean.class, wrappedPrimitive));
        } else if (componentType.isAssignableFrom(byte.class)) {
            Array.setByte(object, index, toWrapperType(Byte.class, wrappedPrimitive));
        } else if (componentType.isAssignableFrom(char.class)) {
            Array.setChar(object, index, toWrapperType(Character.class, wrappedPrimitive));
        } else {
            throw new RuntimeException("Component type '" + componentType.getName() + "' was not a primitive");
        }
    }

    private <T> T toWrapperType(Class<T> targetType, Object wrappedPrimitive) {
        if (targetType.isAssignableFrom(Integer.class)) {
            int toSet = 0;
            if (wrappedPrimitive instanceof Long) {
                Long longPrimitive = (Long) wrappedPrimitive;
                if (longPrimitive > Integer.MAX_VALUE || longPrimitive < Integer.MIN_VALUE) {
                    throw new IllegalArgumentException("Long value '" + longPrimitive + " is out of Integer range");
                }
                toSet = longPrimitive.intValue();
            } else if (wrappedPrimitive instanceof Short) {
                Short shortPrimitive = (Short) wrappedPrimitive;
                toSet = shortPrimitive.intValue();
            } else {
                toSet = (Integer) wrappedPrimitive;
            }
            return targetType.cast(toSet);
        } else if (targetType.isAssignableFrom(Short.class)) {
            short toSet = 0;
            if (wrappedPrimitive instanceof Long) {
                Long longPrimitive = (Long) wrappedPrimitive;
                if (longPrimitive > Short.MAX_VALUE || longPrimitive < Short.MIN_VALUE) {
                    throw new IllegalArgumentException("Long value '" + longPrimitive + " is out of Short range");
                }
                toSet = longPrimitive.shortValue();
            } else if (wrappedPrimitive instanceof Integer) {
                Integer intPrimitive = (Integer) wrappedPrimitive;
                if (intPrimitive > Short.MAX_VALUE || intPrimitive < Short.MIN_VALUE) {
                    throw new IllegalArgumentException("Integer value '" + intPrimitive + " is out of Short range");
                }
                toSet = intPrimitive.shortValue();
            } else {
                toSet = (Short) wrappedPrimitive;
            }
            return targetType.cast(toSet);
        } else if (targetType.isAssignableFrom(Long.class)) {
            long toSet = 0;
            if (wrappedPrimitive instanceof Short) {
                Short shortPrimitive = (Short) wrappedPrimitive;
                toSet = shortPrimitive.longValue();
            } else if (wrappedPrimitive instanceof Integer) {
                Integer intPrimitive = (Integer) wrappedPrimitive;
                toSet = intPrimitive.longValue();
            } else {
                toSet = (Long) wrappedPrimitive;
            }
            return targetType.cast(toSet);
        } else if (targetType.isAssignableFrom(Float.class)) {
            float toSet = 0.0f;
            if (wrappedPrimitive instanceof Double) {
                Double doublePrimitive = (Double) wrappedPrimitive;
                if (doublePrimitive < Float.MIN_VALUE || doublePrimitive > Float.MAX_VALUE) {
                    throw new IllegalArgumentException("Double value '" + doublePrimitive + " is out of Float range");
                }
                toSet = doublePrimitive.floatValue();
            } else {
                toSet = (Float) wrappedPrimitive;
            }
            return targetType.cast(toSet);
        } else if (targetType.isAssignableFrom(Double.class)) {
            double toSet = 0.0;
            if (wrappedPrimitive instanceof Float) {
                Float floatPrimitive = (Float) wrappedPrimitive;
                toSet = floatPrimitive.doubleValue();
            } else {
                toSet = (Double) wrappedPrimitive;
            }
            return targetType.cast(toSet);
        } else if (targetType.isAssignableFrom(Boolean.class)
                || targetType.isAssignableFrom(Byte.class)
                || targetType.isAssignableFrom(Character.class)) {
            return targetType.cast(wrappedPrimitive);
        }

        throw new RuntimeException("Component type '" + targetType.getName() + "' was not a primitive");
    }

    public DynamicObject parse(InputStream inputStream) {
        if (null == inputStream) {
            throw new NullPointerException("inputStream must be non-null and support mark()");
        }
        JsonTokenReader reader = new JsonTokenReader(new BufferedReader(new InputStreamReader(inputStream)));
        return parseJson(reader);
    }

    public DynamicObject parse(String jsonString) {
        if (null == jsonString) {
            throw new NullPointerException("jsonString must be non-null");
        }
        JsonTokenReader reader = new JsonTokenReader(new StringReader(jsonString));
        return parseJson(reader);
    }

    private DynamicObject parseJson(JsonTokenReader reader) {
        JsonToken token = reader.next();
        if (JsonTokenType.ARRAY_START != token.type
                && JsonTokenType.OBJECT_START != token.type) {
            throw new JsonParseException("Root must be json object or array, but was " + token);
        }
        DynamicObject root = parseJsonEntity(reader, token);
        if (null == root) {
            throw new JsonParseException("Root is null");
        }
        if (reader.hasNext()) {
            throw new JsonParseException("Unexpected trailing tokens");
        }
        return root;
    }

    private DynamicObject parseJsonObject(JsonTokenReader reader) {
        DynamicObject object = new DynamicObject(DynamicType.OBJECT);
        JsonToken token;

        do {
            token = reader.next();
            // handles empty object case {}
            if (JsonTokenType.OBJECT_END == token.type) {
                return object;
            }
            validateToken(token, JsonTokenType.STRING);
            String key = token.value;

            token = reader.next();
            validateToken(token, JsonTokenType.OBJECT_ASSIGNMENT);
            token = reader.next();
            DynamicObject value = parseJsonEntity(reader, token);
            object.put(key, value);

            token = reader.next();
            validateToken(token, JsonTokenType.OBJECT_END, JsonTokenType.SEPARATOR);
        } while (JsonTokenType.OBJECT_END != token.type);

        return object;
    }

    private DynamicObject parseJsonArray(JsonTokenReader reader) {
        DynamicObject array = new DynamicObject(DynamicType.ARRAY);
        JsonToken token;

        do {
            token = reader.next();
            // handles empty array case []
            if (JsonTokenType.ARRAY_END == token.type) {
                return array;
            }
            array.add(parseJsonEntity(reader, token));
            token = reader.next();
            validateToken(token, JsonTokenType.ARRAY_END, JsonTokenType.SEPARATOR);
        } while (JsonTokenType.ARRAY_END != token.type);

        return array;
    }

    private void validateToken(JsonToken token, JsonTokenType... expectedTypes) {
        for (JsonTokenType expType : expectedTypes) {
            if (expType == token.type) {
                return;
            }
        }
        throw new JsonParseException("Expected one of " + Arrays.toString(expectedTypes) + " but found " + token);
    }

    private DynamicObject parseJsonEntity(JsonTokenReader reader, JsonToken token) {
        switch (token.type) {
            case ARRAY_START:
                return parseJsonArray(reader);
            case OBJECT_START:
                return parseJsonObject(reader);
            case LITERAL_BOOLEAN:
                return new DynamicObject(DynamicType.VALUE, Boolean.valueOf(token.value));
            case LITERAL_NULL:
                return new DynamicObject(DynamicType.VALUE);
            case STRING:
                return new DynamicObject(DynamicType.VALUE, token.value);
            case LITERAL_NUMBER:
                if (token.value.contains(".") || token.value.contains("e")) {
                    return new DynamicObject(DynamicType.VALUE, Double.valueOf(token.value));
                }
                return new DynamicObject(DynamicType.VALUE, Long.valueOf(token.value));
            default:
                throw new JsonParseException("Expected json value but found: " + token);
        }
    }
}
