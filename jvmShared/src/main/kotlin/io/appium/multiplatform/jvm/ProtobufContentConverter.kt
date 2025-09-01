package io.appium.multiplatform.jvm

import com.google.protobuf.Message
import io.appium.multiplatform.jvm.ReflectiveAccess.Companion.reflectMethod
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import kotlin.reflect.KClass

/**
 * Ktor [ContentConverter] for Google Protocol Buffers.
 *
 * Serializes [Message] to bytes and deserializes bytes to [Message] dynamically
 * using the message class's static `parseFrom(byte[])` method.
 *
 * This converter can be used in both Ktor server and client content negotiation.
 *
 * **Usage Notes:**
 * - Must be registered **before** [io.ktor.serialization.kotlinx.KotlinxSerializationConverter] in the content negotiation plugin,
 *   otherwise it may incorrectly attempt to handle non-protobuf types and throw exceptions.
 * - Required dependencies:
 *   - `com.google.protobuf:protobuf-java`
 *   - `io.ktor:ktor-serialization`
 *   - `io.ktor:ktor-http`
 *   - `io.ktor:ktor-io`
 *   - `io.ktor:ktor-utils`
 *   - `io.github.oshai:kotlin-logging`
 *   - `org.jetbrains.kotlinx:kotlinx-coroutines-core`
 *
 * Example registration (server or client):
 * ```
 * install(ContentNegotiation) {
 *     register(ContentType.Application.ProtoBuf, ProtobufContentConverter())
 *     json() // kotlinx.serialization converter
 * }
 * ```
 */
class ProtobufContentConverter() : ContentConverter {
    private val logger = KotlinLogging.logger {}

    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent? {
        val bytes = when (value) {
            is Message -> value.toByteArray()
            else -> return null
        }
        return ByteArrayContent(
            bytes = bytes,
            contentType = ContentType.Application.ProtoBuf
        )
    }

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any? {
        if (!typeInfo.isProtobuf()) {
            return null
        }
        val bytes = withContext(Dispatchers.IO) { content.readRemaining().readByteArray() }
        val parseFrom = reflectMethod(typeInfo.type.java, "parseFrom", ByteArray::class.java)
        return parseFrom.invokeStatic(bytes)
    }

    companion object {
        /** Checks if a [KClass] is a protobuf message type. */
        fun isProtobuf(kClass: KClass<*>): Boolean {
            return Message::class.java.isAssignableFrom(kClass.java)
        }

        /** Checks if a [TypeInfo] corresponds to a protobuf message. */
        fun TypeInfo.isProtobuf(): Boolean {
            return isProtobuf(type)
        }
    }
}