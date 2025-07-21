package io.appium.multiplatform

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {}
    val resource = Thread.currentThread().contextClassLoader.getResource("application.yaml")
    logger.info { "Starting application at $resource" }
    // TODO: SLF4J(W): No SLF4J providers were found.
    EngineMain.main(args)
}

fun Application.module() {
    routing {
        get("/") {
            call.respondText("Hello, world!")
        }
    }
}