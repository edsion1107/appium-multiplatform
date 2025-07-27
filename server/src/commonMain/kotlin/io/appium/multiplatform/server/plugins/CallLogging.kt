package io.appium.multiplatform.server.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*

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