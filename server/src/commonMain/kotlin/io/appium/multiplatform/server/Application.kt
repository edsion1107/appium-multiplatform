package io.appium.multiplatform.server

import io.appium.multiplatform.init
import io.appium.multiplatform.server.plugins.*
import io.appium.multiplatform.server.routes.webdriverRoutes
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


@Suppress("unused")
private val static = object {
    init {
        init()
    }
}

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.requiredPlugins() {
    configureContentNegotiation()
    configureRequestValidation()
    configureResources()
    configureCallLogging()
}

@Suppress("unused")
fun Application.performancePlugins() {
    configureCachingHeaders()
    configureCompression()
    configureConditionalHeaders()
    configurePartialContent()
}

@Suppress("unused")
fun Application.commonPlugins() {
    configureAutoHeadResponse()
    configureCORS()
    configureDefaultHeaders()
    configureForwardedHeaders()
    configureMicrometerMetrics()
}

fun Application.module() {
    requiredPlugins()
//    performancePlugins()
//    commonPlugins()

    routing {
        get("/") {
            call.respondText("Hello, world!")
        }
        webdriverRoutes()
    }
}