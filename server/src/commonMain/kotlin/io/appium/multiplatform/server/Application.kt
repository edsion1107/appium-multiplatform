package io.appium.multiplatform.server

import io.appium.multiplatform.init
import io.appium.multiplatform.logger
import io.appium.multiplatform.protocol.webdriver.Status
import io.appium.multiplatform.server.plugins.*
import io.github.oshai.kotlinlogging.slf4j.toKLogger
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
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

fun Application.module() {
    configureAutoHeadResponse()
    configureCachingHeaders()
    configureCallLogging()
    configureCompression()
    configureConditionalHeaders()
    configureContentNegotiation()
    configureDefaultHeaders()
    configureResources()
    routing {
        get("/") {
            println(log.toKLogger().name)
            log.toKLogger().debug { "Hello World" }
            println(log.name)
            log.debug("Hello World")
            println(logger.name)
            logger.debug { "Hello World" }
            call.respondText("Hello, world!")
        }
        get<Status> {
            call.respondText(call.request.uri)
        }
    }
}