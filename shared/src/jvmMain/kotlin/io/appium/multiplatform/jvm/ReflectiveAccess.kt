package io.appium.multiplatform.jvm

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Reflective access helper for fields and methods.
 *
 * Provides caching for better performance and supports single-line creation similar to the old
 * ReflectiveMethod/ReflectiveField classes.
 *
 * ## Kotlin Usage
 *
 * ```kotlin
 * val parseMethod = reflectMethod(typeInfo.type.java, "parseFrom", ByteArray::class.java)
 * val message = parseMethod.invoke(null, byteArray) as Message
 *
 * val nameField = reflectField(User::class.java, "name")
 * val user = User("Alice")
 * println(nameField.get(user))  // "Alice"
 * nameField.set(user, "Bob")
 * println(nameField.get(user))  // "Bob"
 * ```
 *
 * ## Java Usage
 *
 * ```java
 * ReflectiveAccess.MethodAccess parseMethod =
 *     ReflectiveAccess.reflectMethod(typeInfo.type.java, "parseFrom", byte[].class);
 * Message message = (Message) parseMethod.invoke(null, byteArray);
 *
 * ReflectiveAccess.FieldAccess nameField =
 *     ReflectiveAccess.reflectField(User.class, "name");
 * User user = new User("Alice");
 * System.out.println(nameField.get(user));  // "Alice"
 * nameField.set(user, "Bob");
 * System.out.println(nameField.get(user));  // "Bob"
 * ```
 */
class ReflectiveAccess private constructor(private val clazz: Class<*>?) {

    companion object {

        /** Single-line factory for methods (Java + Kotlin) */
        @JvmStatic
        fun reflectMethod(clazz: Class<*>, methodName: String, vararg paramTypes: Class<*>): MethodAccess =
            MethodAccess(clazz, methodName, paramTypes)

        /** Single-line factory for fields (Java + Kotlin) */
        @JvmStatic
        fun reflectField(clazz: Class<*>, fieldName: String): FieldAccess =
            FieldAccess(clazz, fieldName)

        /** Exception wrapper for reflection errors */
        class ReflectionException(cause: Throwable) : RuntimeException(cause)

    }

    /** Reflective method accessor with caching */
    class MethodAccess(
        private val clazz: Class<*>,
        private val methodName: String,
        private val paramTypes: Array<out Class<*>>
    ) {
        @Volatile
        private var initialized = false
        private var method: Method? = null

        /**
         * Invoke the method.
         *
         * @param receiver object instance, or null for static methods
         * @param args method arguments
         * @return result of the method as Any? (caller should cast)
         * @throws ReflectionException on reflection error
         */
        @Throws(ReflectionException::class)
        fun invoke(receiver: Any?, vararg args: Any?): Any? {
            try {
                initIfNecessary()
                return method!!.invoke(receiver, *args)
            } catch (e: ReflectiveOperationException) {
                throw ReflectionException(e)
            }
        }

        /** Invoke static method */
        fun invokeStatic(vararg args: Any?): Any? = invoke(null, *args)

        @Synchronized
        private fun initIfNecessary() {
            if (initialized) return
            method = clazz.getDeclaredMethod(methodName, *paramTypes).apply { isAccessible = true }
            initialized = true
        }
    }

    /** Reflective field accessor with caching */
    class FieldAccess(
        private val clazz: Class<*>,
        private val fieldName: String
    ) {
        @Volatile
        private var initialized = false
        private var field: Field? = null

        /**
         * Get field value.
         *
         * @param receiver object instance
         * @return value as Any? (caller should cast)
         * @throws ReflectionException on reflection error
         */
        @Throws(ReflectionException::class)
        fun get(receiver: Any?): Any? {
            try {
                initIfNecessary()
                return field!!.get(receiver)
            } catch (e: ReflectiveOperationException) {
                throw ReflectionException(e)
            }
        }

        /**
         * Set field value.
         *
         * @param receiver object instance
         * @param value value to set
         * @throws ReflectionException on reflection error
         */
        @Throws(ReflectionException::class)
        fun set(receiver: Any?, value: Any?) {
            try {
                initIfNecessary()
                field!!.set(receiver, value)
            } catch (e: ReflectiveOperationException) {
                throw ReflectionException(e)
            }
        }

        @Synchronized
        private fun initIfNecessary() {
            if (initialized) return
            field = clazz.getDeclaredField(fieldName).apply { isAccessible = true }
            initialized = true
        }
    }
}

