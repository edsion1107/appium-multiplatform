package io.appium.multiplatform.server.plugins

import io.appium.multiplatform.defaultJson
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.resources.Resources
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset

suspend fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(defaultJson)
    }
}
suspend fun Application.configureRequestValidation() {
    install(RequestValidation) {
    }
}
suspend fun Application.configureResources() {
    install(Resources)
}
suspend fun Application.configureCallLogging() {
    install(CallLogging) {
        disableDefaultColors() // jansi not support android .
//        level = Level.INFO // ⚠️not config global slf4j log level
//        logger = KtorSimpleLogger(io.appium.multiplatform.logger.name)
        // The framework and plugin use slf4j: Application.log(environment.log),
        // and the business code uses KotlinLogging: io.appium.multiplatform.logger
        // sample: log.toKLogger().info { "Hello World" } or io.appium.multiplatform.logger.debug { "Hello World" }
    }
}
suspend fun Application.requiredPlugins() {
    configureContentNegotiation()
    configureRequestValidation()
    configureResources()
    configureCallLogging()
}

class ProtobufConverter(): ContentConverter {
    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent? {
        TODO("Not yet implemented")
    }

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any? {
        TODO("Not yet implemented")
    }

}