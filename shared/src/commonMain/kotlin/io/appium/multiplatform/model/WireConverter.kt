package io.appium.multiplatform.model

import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
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
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties

/**
 * Ktor ContentConverter for Wire messages
 */
class WireConverter : ContentConverter {
    /**
     * Adapter cache to avoid repeated reflection
     */
    private val cache = Cache.Builder<TypeInfo, ProtoAdapter<Message<*, *>>>().build()
    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent {
        val message = value as? Message<*, *>
            ?: throw WireException.Encoding(
                "Unsupported type for serialization: $typeInfo"
            )
        return try {
            val bytes = typeInfo.getAdapter().encode(message)
            if (bytes.isEmpty()) {
                throw WireException.Encoding("Encoding produced empty bytes for $typeInfo")
            }
            ByteArrayContent(
                bytes = bytes,
                contentType = ContentType.Application.ProtoBuf
            )
        } catch (e: Exception) {
            throw WireException.Encoding(
                "Failed to encode ${typeInfo}: ${e.message}", e
            )
        }
    }

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any {
        return try {
            val bytes = withContext(Dispatchers.IO) { content.readRemaining().readByteArray() }
            if (bytes.isEmpty()) {
                throw WireException.Decoding("Empty body received for ${typeInfo.type}")
            }
            typeInfo.getAdapter().decode(bytes)
        } catch (e: Exception) {
            throw WireException.Decoding(
                "Failed to decode ${typeInfo.type}: ${e.message}", e
            )
        }
    }


    /**
     * Gets the corresponding ProtoAdapter based on the type
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun TypeInfo.getAdapter(): ProtoAdapter<Message<*, *>> {
        if (!this.isWireMessage()) {
            throw WireException.AdapterNotFound("Type $this is not a Wire Message")
        }
        return cache.get(this) {
            val companion = type.companionObjectInstance
                ?: throw WireException.AdapterNotFound("Companion object not found for $this")

            val adapterProp = type.companionObject
                ?.declaredMemberProperties
                ?.firstOrNull { it.name == "ADAPTER" }
                ?: throw WireException.AdapterNotFound("No ADAPTER property in companion of $this")

            adapterProp.getter.call(companion) as? ProtoAdapter<Message<*, *>>
                ?: throw WireException.AdapterNotFound("Invalid ADAPTER type for $this")

        }
    }

    companion object {
        fun isWireMessage(kClass: KClass<*>): Boolean =
            kClass == Message::class || kClass.supertypes.any {
                (it.classifier as? KClass<*>)?.let(::isWireMessage) == true
            }

        fun TypeInfo.isWireMessage(): Boolean = isWireMessage(this.type)

        /**
         * Unified encapsulation of Wire exceptions
         */
        sealed class WireException(message: String?, cause: Throwable? = null) :
            SerializationException(message, cause) {

            class Decoding(message: String?, cause: Throwable? = null) : WireException(message, cause)
            class Encoding(message: String?, cause: Throwable? = null) : WireException(message, cause)
            class AdapterNotFound(message: String?, cause: Throwable? = null) : WireException(message, cause)
        }
    }
}
