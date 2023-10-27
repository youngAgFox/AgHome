package com.ag.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class field as a field to initialize by the JsonInitializer.
 * <p>
 * The annotation can specify the name of the json field to set this field to,
 * or when left empty or supplied the empty String "", will default to the field name.
 * <p>
 * Examples:
 * <pre>
 * {@code @AutoInit}
 * private int foo;
 * </pre>
 * <pre>
 * {@code @AutoInit("bar")}
 * private int foo; // foo will be initialized from the Json model field 'bar' instead of 'foo'
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoInit {
    // can specify the field name to look for. When empty string, uses field name as key.
    public String getFieldName() default "";
}
