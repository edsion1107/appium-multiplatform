package io.appium.multiplatform.model

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
import java.lang.reflect.Method
import kotlin.reflect.KClass

class ProtobufContentConverter : ContentConverter {
    private val logger = KotlinLogging.logger {}
    private val cache = Cache.Builder<TypeInfo, Method>().build()
    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent? {
        if (!isProtobuf(typeInfo)) {
            throw ProtobufException.Encoding("Protobuf doesn't support protobuf types: $value")
        } else if (value == null) {
            throw ProtobufException.Encoding("Encoding produced null for ${typeInfo.type}")
        } else {
            val bytes = (value as? Message)?.toByteArray() ?: byteArrayOf()
            if (bytes.isEmpty()) {
                throw ProtobufException.Encoding("Encoding produced empty bytes for ${typeInfo.type}")
            }
            return ByteArrayContent(
                bytes = bytes,
                contentType = ContentType.Application.ProtoBuf
            )
        }
    }

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any? {
        if (!isProtobuf(typeInfo)) {
            throw ProtobufException.Decoding("Protobuf doesn't support protobuf types: $typeInfo")
        }
        val bytes = withContext(Dispatchers.IO) { content.readRemaining().readByteArray() }
        if (bytes.isEmpty()) {
            throw ProtobufException.Encoding("Empty body received for $typeInfo")
        }
        val parseFrom = cache.get(typeInfo) {
            typeInfo.type.java.getMethod("parseFrom", ByteArray::class.java)
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
            class AdapterNotFound(message: String?, cause: Throwable? = null) : ProtobufException(message, cause)
        }
    }
}