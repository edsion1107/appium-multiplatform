package io.appium.multiplatform.server

import io.appium.multiplatform.logger
import io.appium.multiplatform.request.WebdriverSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.resources.*
import io.ktor.server.sessions.*

expect fun requestValidation(): RequestValidationConfig.() -> Unit
expect fun contentNegotiation(): ContentNegotiationConfig.() -> Unit
fun Application.configureContentNegotiation() {
    install(ContentNegotiation, contentNegotiation())
}

fun Application.configureRequestValidation() {
    install(RequestValidation, requestValidation())
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

fun Application.configureSessions() {
    install(Sessions) {
        cookie<WebdriverSession>("webdriver_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60
        }
    }
    install(Authentication) {
        session<WebdriverSession>("webdriver_auth") {
            validate { session ->
                //TODO: not work
                logger.info { "session validated: $session" }
                session
            }
        }
    }
}
