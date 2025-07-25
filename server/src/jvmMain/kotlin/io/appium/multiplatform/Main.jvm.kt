package io.appium.multiplatform

import io.appium.multiplatform.jvm.configSlf4j
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json

actual val defaultJson: Json by lazy {
    Json {
        prettyPrint = true
        isLenient = true
    }
}

actual val logger: KLogger = run {
    configSlf4j()
    KotlinLogging.logger {}
}

actual fun init() {
    logger.debug { "init" }
    Thread.currentThread().contextClassLoader.getResource("application.yaml")
        ?.let { logger.info { "Starting application at $it" } }
}