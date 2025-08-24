package io.appium.multiplatform.model

import kotlinx.serialization.SerializationException

/**
 * Unified encapsulation of Wire exceptions
 */
sealed class WireException(message: String?, cause: Throwable? = null) :
    SerializationException(message, cause) {

    class Decoding(message: String?, cause: Throwable? = null) : WireException(message, cause)
    class Encoding(message: String?, cause: Throwable? = null) : WireException(message, cause)
    class AdapterNotFound(message: String?, cause: Throwable? = null) : WireException(message, cause)
}