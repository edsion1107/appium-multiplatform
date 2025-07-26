package io.appium.multiplatform

import io.appium.multiplatform.jvm.configSlf4j
import io.appium.multiplatform.jvm.setSystemPropertyIfAbsent
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
    setSystemPropertyIfAbsent("kotlin-logging-to-android-native", "true")
    configSlf4j()
    KotlinLogging.logger {}
    // set loglevel for KLogger(Android logcat):
    // `adb shell setprop log.tag.io.appium.multiplatform.Main_android VERBOSE`
}

actual fun init() {
    logger.debug { "init" }
    Thread.currentThread().contextClassLoader?.getResource("application.yaml")
        ?.let { logger.info { "Starting application at $it" } }
}

