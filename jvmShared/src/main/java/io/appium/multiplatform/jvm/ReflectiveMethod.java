package io.appium.multiplatform.jvm;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper class for making more performant reflection method invocations.
 *
 * <p>Lazy initializes and caches Method object to attempt to reduce reflection overhead.
 */
public class ReflectiveMethod<T> {
    private final String className;
    private final String methodName;
    private final Class<?>[] paramTypes;
    private final Class<?> clazz;

    // lazy init
    private boolean initialized = false;
    private Method method;

    /**
     * Creates a io.appium.multiplatform.jvm.ReflectiveMethod.
     *
     * @param className  the fully qualified class name that defines the method
     * @param methodName the method name to call
     * @param paramTypes the list of types of the method parameters, in order.
     */
    public ReflectiveMethod(String className, String methodName, Class<?>... paramTypes) {
        this.className = className;
        this.clazz = null;
        this.paramTypes = paramTypes;
        this.methodName = methodName;
    }

    public ReflectiveMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        this.className = null;
        this.clazz = clazz;
        this.paramTypes = paramTypes;
        this.methodName = methodName;
    }

    /**
     * Invoke the instance method.
     *
     * <p>See {@link java.lang.reflect.Method#invoke(Object, Object...)}
     *
     * @param object      the object the underlying method is invoked from
     * @param paramValues the arguments used for the method call
     * @return the return value of the method
     * @throws ReflectionException if call could not be completed
     */
    public T invoke(Object object, Object... paramValues) throws ReflectionException {
        try {
            initIfNecessary();
            return (T) method.invoke(object, paramValues);
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Invoke th static method.
     *
     * <p>See {@link java.lang.reflect.Method#invoke(Object, Object...)}
     *
     * @param paramValues the arguments used for the method call
     * @return the return value of the method
     * @throws ReflectionException if call could not be completed
     */
    public T invokeStatic(Object... paramValues) throws ReflectionException {
        return invoke(null, paramValues);
    }

    private synchronized void initIfNecessary() throws ClassNotFoundException, NoSuchMethodException {
        if (initialized) {
            return;
        }
        method = getClazz().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        initialized = true;
    }

    private Class<?> getClazz() throws ClassNotFoundException {
        if (clazz == null) {
            assert className != null;
            return Class.forName(className);
        } else {
            return clazz;
        }
    }
}