package io.appium.multiplatform.jvm;

/**
 * Thrown when there was a failure making a reflective call.
 */
public class ReflectionException extends RuntimeException {

    ReflectionException(Exception cause) {
        super("Reflection access failed", cause);
    }
}