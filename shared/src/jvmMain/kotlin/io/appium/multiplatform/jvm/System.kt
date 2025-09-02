package io.appium.multiplatform.jvm


/**
 * Sets the system property [key] to the specified [value] **only if** the property
 * is not already set or is blank.
 *
 * This is useful for assigning default values without overriding
 * any user-provided or preconfigured system property.
 *
 * Blank means the property is either `null`, empty, or contains only whitespace.
 *
 * Example usage:
 * ```
 * setSystemPropertyIfAbsent("my.property", "defaultValue")
 * ```
 *
 * @param key the name of the system property
 * @param value the default value to assign if the property is absent or blank
 *
 * @see java.lang.System.getProperty
 * @see java.lang.System.setProperty
 */
fun setSystemPropertyIfAbsent(key: String, value: String) {
    if (System.getProperty(key, null).isNullOrBlank()) {
        System.setProperty(key, value)
    }
}