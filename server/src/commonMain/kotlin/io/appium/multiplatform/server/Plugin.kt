package io.appium.multiplatform.server

import io.appium.multiplatform.defaultJson
import io.appium.multiplatform.model.WireConverter
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.resources.Resources

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        checkAcceptHeaderCompliance = true
        // The order of registered ContentConverters affects the matching logic.
        // If KotlinxSerializationConverter is placed first, it will be selected with higher priority during checks.
        register(ContentType.Application.ProtoBuf, WireConverter())
        json(defaultJson)
    }
}

fun Application.configureRequestValidation() {
    install(RequestValidation) {
    }
}

fun Application.configureResources() {
    install(Resources)
}

fun Application.configureCallLogging() {
    install(CallLogging) {
        disableDefaultColors() // jansi not support android .
//        level = Level.INFO // ⚠️not config global slf4j log level
//        logger = KtorSimpleLogger(io.appium.multiplatform.logger.name)
        // The framework and plugin use slf4j: Application.log(environment.log),
        // and the business code uses KotlinLogging: io.appium.multiplatform.logger
        // sample: log.toKLogger().info { "Hello World" } or io.appium.multiplatform.logger.debug { "Hello World" }
    }
}
fun Application.configureAutoHeadResponse() {
    install(AutoHeadResponse)
}

fun Application.configureCORS() {
    install(CORS) {
        allowHost("localhost")
    }
}

fun Application.configureDefaultHeaders() {
    install(DefaultHeaders)
}

fun Application.configureForwardedHeaders() {
    install(ForwardedHeaders) {
    }
}

fun Application.configureMicrometerMetrics() {
    // TODO: Waiting for bug fix compilation(JvmGcMetrics)
//    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
//    install(MicrometerMetrics){
//        registry = appMicrometerRegistry
//        meterBinders = emptyList()
//    }
//    routing {
//        get("/metrics") {
//            call.respond(appMicrometerRegistry.scrape())
//        }
//    }
}
fun Application.configureCachingHeaders() {
    install(CachingHeaders) {
    }
}

fun Application.configureCompression() {
    install(Compression) {
        deflate()
        gzip()
    }
}

fun Application.configureConditionalHeaders() {
    install(ConditionalHeaders) {
    }
}

fun Application.configurePartialContent() {
    install(PartialContent) {
    }
}
