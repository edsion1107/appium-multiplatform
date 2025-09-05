package io.appium.multiplatform.jvm

import com.google.type.Date
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*

/**
 * @see ProtobufContentConverter
 */
class ProtobufContentConverterTest : FunSpec() {
    val converter = ProtobufContentConverter()
    val data: Date = Date.newBuilder().apply {
        setDay(1)
        setMonth(1)
        setYear(2000)
    }.build()
    val protoTypeInfo = typeInfo<Date>()

    init {
        test("serialize Protobuf") {
            val result = converter.serialize(
                ContentType.Application.ProtoBuf,
                Charsets.UTF_8,
                protoTypeInfo,
                data
            )
            result.shouldNotBeNull()
            result.status.shouldBeNull()
            result.headers.shouldBe(Headers.Empty)
            result.contentType.shouldBe(ContentType.Application.ProtoBuf)
            result.shouldBeInstanceOf<ByteArrayContent>()
            result.contentLength.shouldBeEqual(data.serializedSize.toLong())
        }
        test("serialize does not throw an exception") {
            val result = converter.serialize(
                ContentType.Application.ProtoBuf,
                Charsets.UTF_8,
                protoTypeInfo,
                this
            )
            result.shouldBeNull()
        }

        test("deserialize Protobuf") {
            val result =
                converter.deserialize(Charsets.UTF_8, protoTypeInfo, ByteReadChannel(content = data.toByteArray()))
            result.shouldNotBeNull()
            result.shouldBeInstanceOf<Date>()
            result.shouldBe(data)
        }
        test("deserialize does not throw an exception") {
            converter.deserialize(Charsets.UTF_8, typeInfo<String>(), ByteReadChannel(byteArrayOf())).also {
                it.shouldBeNull()
            }
            converter.deserialize(Charsets.UTF_16, typeInfo<String>(), ByteReadChannel(byteArrayOf())).also {
                it.shouldBeNull()
            }
            converter.deserialize(Charsets.UTF_16, typeInfo<ByteArray>(), ByteReadChannel(byteArrayOf())).also {
                it.shouldBeNull()
            }
        }
    }
}
