package io.appium.multiplatform.server.plugins

import io.ktor.server.application.*

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