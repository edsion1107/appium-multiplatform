package io.appium.multiplatform

import io.github.oshai.kotlinlogging.KLogger
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

@Suppress("unused")
private val static = object {
    init {
        init()
    }
}

/**
 * Performs platform-specific initialization and pre-checks.
 *
 * ⚠️ Due to the uncertain initialization order of top-level properties,
 * avoid directly initializing top-level values that depend on init logic.
 * Instead, prefer using `by lazy` to ensure deferred and safe initialization.
 *
 * For example:
 *
 * ```
 * // File: Main.android.kt
 * actual fun init() {
 *     System.setProperty("kotlin-logging-to-android-native", "true")
 * }
 *
 * // Bad: may be initialized before `init()` is called
 * actual val logger = KotlinLogging.logger {}
 *
 * // Good: ensures logger is initialized after `init()` is executed
 * actual val logger: KLogger by lazy { KotlinLogging.logger {} }
 * ```
 */
expect fun init()

expect val logger: KLogger

expect val defaultJson: Json


fun main(args: Array<String>) {
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