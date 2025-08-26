package io.appium.multiplatform.jvm

import com.google.protobuf.Message
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.reactivecircus.cache4k.Cache
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import kotlinx.serialization.SerializationException
import kotlin.reflect.KClass

class ProtobufContentConverter : ContentConverter {
    private val logger = KotlinLogging.logger {}

    // Only the deserialization method `parseFrom` is obtained via reflection and therefore needs to be cached,
    // while serialization directly uses `com.google.protobuf.MessageLite.toByteArray`.
    private val cache = Cache.Builder<TypeInfo, ReflectiveMethod<Message>>().build()
    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent? {
        if (!isProtobuf(typeInfo)) {
            return null
        }
        val bytes = (value as? Message)?.toByteArray()
            ?: throw ProtobufException.Encoding("Protobuf doesn't support protobuf types: $value")
        if (bytes.isEmpty()) {
            throw ProtobufException.Encoding("Encoding produced empty bytes for ${typeInfo.type}")
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
        if (!isProtobuf(typeInfo)) {
            return null
        }
        val bytes = withContext(Dispatchers.IO) { content.readRemaining().readByteArray() }
        if (bytes.isEmpty()) {
            throw ProtobufException.Decoding("Empty body received for $typeInfo")
        }
        val parseFrom = cache.get(typeInfo) {
            ReflectiveMethod<Message>(typeInfo.type.java, "parseFrom", ByteArray::class.java)
        }
        return parseFrom.invoke(null, bytes)
    }

    companion object {
        fun isProtobuf(kClass: KClass<*>): Boolean {
            return Message::class.java.isAssignableFrom(kClass.java)
        }

        fun isProtobuf(typeInfo: TypeInfo): Boolean {
            return isProtobuf(typeInfo.type)
        }

        sealed class ProtobufException(message: String?, cause: Throwable? = null) :
            SerializationException(message, cause) {
            class Decoding(message: String?, cause: Throwable? = null) : ProtobufException(message, cause)
            class Encoding(message: String?, cause: Throwable? = null) : ProtobufException(message, cause)
        }
    }
}