package io.appium.multiplatform.jvm;

import java.lang.reflect.Field;

/**
 * Helper class for making more performant reflection field access.
 *
 * <p>Lazy initializes and caches Method object ro attempt to reduce reflection overhead.
 */
public class ReflectiveField<T> {
    private final String className;
    private final String fieldName;
    private final Class<?> clazz;

    // lazy init
    private boolean initialized = false;
    private Field field;

    /**
     * Creates a io.appium.multiplatform.jvm.ReflectiveField.
     *
     * @param className the fully qualified class name that defines the field
     * @param fieldName the field name
     */
    public ReflectiveField(String className, String fieldName) {
        this.clazz = null;
        this.className = className;
        this.fieldName = fieldName;
    }

    public ReflectiveField(Class<?> clazz, String fieldName) {
        this.clazz = clazz;
        this.className = null;
        this.fieldName = fieldName;
    }

    /**
     * Retrieves the field's value, initializing if necessary.
     *
     * @param object the object that holds the field's value
     * @return the field's value
     * @throws ReflectionException if field could not be accessed
     */
    public T get(Object object) throws ReflectionException {
        try {
            initIfNecessary();
            return (T) field.get(object);
        } catch (ClassNotFoundException e) {
            throw new ReflectionException(e);
        } catch (NoSuchFieldException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }

    private synchronized void initIfNecessary() throws ClassNotFoundException, NoSuchFieldException {
        if (initialized) {
            return;
        }
        field = getClazz().getDeclaredField(fieldName);
        field.setAccessible(true);
        initialized = true;
    }

    private Class<?> getClazz() throws ClassNotFoundException {
        if (clazz == null) {
            return Class.forName(className);
        } else {
            return clazz;
        }
    }
}
