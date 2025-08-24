package io.appium.multiplatform.model

import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.readByteArray
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties

/**
 * Ktor ContentConverter for Wire messages
 */
class WireConverter : ContentConverter {

    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent {
        val message = value as? Message<*, *>
            ?: throw WireException.Encoding(
                "Unsupported type for serialization: ${typeInfo.type}"
            )

        val adapter = getAdapterForType(typeInfo.type)

        return try {
            val bytes = adapter.encode(message)
            if (bytes.isEmpty()) {
                throw WireException.Encoding("Encoding produced empty bytes for ${typeInfo.type}")
            }
            ByteArrayContent(
                bytes = bytes,
                contentType = ContentType.Application.ProtoBuf
            )
        } catch (e: Exception) {
            throw WireException.Encoding(
                "Failed to encode ${typeInfo.type}: ${e.message}", e
            )
        }
    }

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any {
        val adapter = getAdapterForType(typeInfo.type)

        return try {
            val bytes = content.readRemaining().readByteArray()
            if (bytes.isEmpty()) {
                throw WireException.Decoding("Empty body received for ${typeInfo.type}")
            }
            adapter.decode(bytes)
        } catch (e: Exception) {
            throw WireException.Decoding(
                "Failed to decode ${typeInfo.type}: ${e.message}", e
            )
        }
    }

    /**
     * Checks if TypeInfo is a WireMessage
     */
    private fun TypeInfo.isWireMessage(): Boolean =
        isWireMessageKClass(type)

    /**
     * Adapter cache to avoid repeated reflection
     */
    private val adapterCache = ConcurrentHashMap<KClass<*>, ProtoAdapter<out Message<*, *>>>()

    /**
     * Gets the corresponding ProtoAdapter based on the type
     */
    @Suppress("UNCHECKED_CAST")
    private fun getAdapterForType(type: KClass<*>): ProtoAdapter<Message<*, *>> {
        if (!isWireMessageKClass(type)) {
            throw WireException.AdapterNotFound("Type $type is not a Wire Message")
        }

        return adapterCache.getOrPut(type) {
            val companion = type.companionObjectInstance
                ?: throw WireException.AdapterNotFound("Companion object not found for $type")

            val adapterProp = type.companionObject
                ?.declaredMemberProperties
                ?.firstOrNull { it.name == "ADAPTER" }
                ?: throw WireException.AdapterNotFound("No ADAPTER property in companion of $type")

            adapterProp.getter.call(companion) as? ProtoAdapter<out Message<*, *>>
                ?: throw WireException.AdapterNotFound("Invalid ADAPTER type for $type")
        } as ProtoAdapter<Message<*, *>>
    }
}
