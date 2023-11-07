package com.ag.json;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.ag.util.ArrayUtils;
import com.ag.util.StringUtils;

public class JsonHelper {
    
    /**
     * Gets the {@code @JsonField} annotation associated with the field, and returns
     * the fieldName it was set with. When set to the default (null or empty string),
     * the field's name is returned instead. When the annotation does not exist, null is
     * returned.
     * 
     * @param field
     * @return the String field name to use to init this field per the Json annotations,
     *         or null when there is no annotation and no class wide annotation.
     */
    public static String getJsonFieldName(Class<?> cls, Field field) {
        // if the field is transient, ignore
        if (Modifier.isTransient(field.getModifiers())) {
            return null;
        }
        Annotation[] annotations = field.getAnnotations();
        Annotation annotation = ArrayUtils.find(annotations,
                ele -> ele.annotationType().isAssignableFrom(JsonField.class));
        if (null == annotation) {
            // when the JsonClass annotation is present or the object implements serializable the field level annotation becomes optional.
            return cls.isAnnotationPresent(JsonClass.class) || Serializable.class.isAssignableFrom(cls) ? field.getName() : null;
        }
        JsonField ai = (JsonField) annotation;
        String key = ai.getFieldName();
        if (StringUtils.isEmptyOrNull(key)) {
            key = field.getName();
        }
        return key;
    }
}
