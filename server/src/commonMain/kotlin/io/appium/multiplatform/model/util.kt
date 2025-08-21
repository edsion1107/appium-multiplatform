package io.appium.multiplatform.model

import com.squareup.wire.Message
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.readByteArray
import kotlinx.serialization.SerializationException
import kotlin.reflect.KClass
import kotlin.reflect.KType

fun isWireMessageKClass(kClass: KClass<*>): Boolean {
    if (kClass == Message::class) return true
    return kClass.supertypes.any { superType: KType ->
        val superClassifier = superType.classifier as? KClass<*>
        superClassifier != null && isWireMessageKClass(superClassifier)
    }
}

class ProtobufConverter() : ContentConverter {
    private val logger = KotlinLogging.logger { }
    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent? {
        if (!typeInfo.isWireMessage()) {
            throw WireMessageDecodingException("Unsupported message type: ${typeInfo.type}")
        } else {
            return ByteArrayContent(
                bytes = (value as Message<*, *>).encode(),
                contentType = ContentType.Application.ProtoBuf,
            )
        }
    }

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Message<*, *> {
        if (!typeInfo.isWireMessage()) {
            throw WireMessageDecodingException("Unsupported message type: ${typeInfo.type}")
        } else {
            return (typeInfo.type as Message<*, *>).adapter.decode(content.readBuffer().readByteArray())
        }
    }

    private fun TypeInfo.isWireMessage(): Boolean {
        return isWireMessageKClass(type)
    }

}


open class WireMessageException(message: String) : SerializationException(message)
class WireMessageDecodingException(message: String) : WireMessageException(message)
class WireMessageEncodingException(message: String) : WireMessageException(message)