package android.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Retention(CLASS)
@Target({METHOD})
public @interface EnforcePermission {
    String value() default "";

    String[] allOf() default {};

    String[] anyOf() default {};
}