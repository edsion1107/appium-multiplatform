package io.appium.multiplatform

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json

actual val defaultJson: Json by lazy {
    Json {
        prettyPrint = true
        isLenient = true
    }
}
actual val logger: KLogger by lazy { KotlinLogging.logger {} }


actual fun init() {
    System.setProperty("kotlin-logging-to-android-native", "true")
    System.setProperty("org.slf4j.simpleLogger.logFile", "System.out")

    Thread.currentThread().contextClassLoader.getResource("application.yaml")
        ?.let { logger.info { "Starting application at $it" } }
}