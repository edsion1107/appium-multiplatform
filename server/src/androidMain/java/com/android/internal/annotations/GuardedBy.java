package com.android.internal.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Annotation type used to mark a method or field that can only be accessed when
 * holding the referenced locks.
 */
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface GuardedBy {
    /**
     * Specifies a list of locks to be held in order to access the field/method
     * annotated with this; when used in conjunction with the {@link CompositeRWLock}, locks
     * should be acquired in the order of the appearance in the {@link #value} here.
     *
     * <p>
     * If specified, {@link #anyOf()} must be null.
     * </p>
     *
     * @see CompositeRWLock
     */
    String[] value() default {};

    /**
     * Specifies a list of locks where at least one of them must be held in order to access
     * the field/method annotated with this; it should be <em>only</em> used in the conjunction
     * with the {@link CompositeRWLock}.
     *
     * <p>
     * If specified, {@link #allOf()} must be null.
     * </p>
     *
     * @see CompositeRWLock
     */
    String[] anyOf() default {};
}