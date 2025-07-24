package io.appium.multiplatform

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import org.slf4j.event.Level

val logger = KotlinLogging.logger {}

fun init() {
    Thread.currentThread().contextClassLoader.getResource("application.yaml")
        ?.let { logger.info { "Starting application at $it" } }
}

fun main(args: Array<String>) {
    init()
    EngineMain.main(args)
}

fun Application.module() {
    install(CallLogging) {
        disableDefaultColors() // jansi not support android .
        level = Level.INFO
        logger = KtorSimpleLogger(io.appium.multiplatform.logger.name)
        // The framework and plugin use slf4j: Application.log(environment.log),
        // and the business code uses KotlinLogging: io.appium.multiplatform.logger
    }
    routing {
        get("/") {
            call.respondText("Hello, world!")
        }
    }
}