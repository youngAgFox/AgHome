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
import com.ag.InvalidDynamicTypeOperationException;

import util.ArrayUtils;
import util.DateUtils;
import util.StringUtils;

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

    public void initialize(Class<?> cls, String jsonString) {
        if (null == jsonString) {
            throw new NullPointerException("jsonString must be non-null");
        }
        JsonTokenReader reader = new JsonTokenReader(new StringReader(jsonString));
        initialize(cls, reader);
    }

    public void initialize(Class<?> cls, InputStream inputStream) {
        if (null == inputStream) {
            throw new NullPointerException("inputStream must be non-null and support mark()");
        }
        JsonTokenReader reader = new JsonTokenReader(new BufferedReader(new InputStreamReader(inputStream)));
        initialize(cls, reader);
    }

    private void initialize(Class<?> cls, JsonTokenReader reader) {
        try {
            Object object = cls.getConstructor().newInstance();
            initializeInstance(object, parseJson(reader));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
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

        if (DynamicType.OBJECT == model.getType()) {
            initializeObject(object, model);
        } else {
            throw new InvalidDynamicTypeOperationException("Cannot initialize from invalid root model (root is not array or object json type)");
        }
    }

    private void initializeObject(Object object, DynamicObject model) 
            throws IllegalArgumentException, IllegalAccessException, InstantiationException, 
                InvocationTargetException, NoSuchMethodException, SecurityException {

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
                    if (field.getType().isArray()) {
                        int len = fieldEntity.size();
                        Object arr = Array.newInstance(field.getType(), len);
                        for (int i = 0; i < len; i++) {
                            if (field.getType().getComponentType().isPrimitive()) {
                                setPrimitiveArray(field, object, i, fieldEntity.get(i));
                            } else {
                                Array.set(arr, i, fieldEntity.get(i));
                            }
                        }
                        field.set(object, arr);
                    } if (field.getType().isAssignableFrom(Collection.class)) {
                        List<Object> arrList = new ArrayList<>();
                        for (DynamicObject dynamicObject : fieldEntity) {
                            arrList.add(dynamicObject.get());
                        }
                        field.set(fieldEntity, arrList);
                    } else {
                        throw new JsonInitializeException("Cannot initialize array with provided fields and types (" + field.getType().getTypeName() + ")");
                    }
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

    /**
     * Gets the {@code @AutoInit} annotation associated with the field, and returns the
     * fieldName it was set with. When set to the default (null or empty string), the field's
     * name is returned instead. When the annotation does not exist, null is returned.
     * 
     * @param field
     * @return the String field name to use to init this field per the annotation, or null when 
     * there is no annotation.
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

    private void setPrimitiveArray(Field field, Object object, int index, Object wrappedPrimitive) {
        if (field.getType() == int.class) {
            Array.setInt(object, index, (Integer) wrappedPrimitive);
        } else if (field.getType() == short.class) {
            Array.setShort(object, index, (Short) wrappedPrimitive);
        } else if (field.getType() == long.class) {
            Array.setLong(object, index, (Long) wrappedPrimitive);
        } else if (field.getType() == float.class) {
            Array.setFloat(object, index, (Float) wrappedPrimitive);
        } else if (field.getType() == double.class) {
            Array.setDouble(object, index, (Double) wrappedPrimitive);
        } else if (field.getType() == boolean.class) {
            Array.setBoolean(object, index, (Boolean) wrappedPrimitive);
        } else if (field.getType() == byte.class) {
            Array.setByte(object, index, (Byte) wrappedPrimitive);
        } else if (field.getType() == char.class) {
            Array.setChar(object, index, (Character) wrappedPrimitive);
        }
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
