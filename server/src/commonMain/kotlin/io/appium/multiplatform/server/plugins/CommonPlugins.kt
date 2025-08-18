package io.appium.multiplatform.server.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*

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

@Suppress("unused")
fun Application.commonPlugins() {
    configureAutoHeadResponse()
    configureCORS()
    configureDefaultHeaders()
    configureForwardedHeaders()
    configureMicrometerMetrics()
}