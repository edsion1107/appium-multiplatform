package io.appium.multiplatform

import io.github.oshai.kotlinlogging.KLogger
import kotlinx.serialization.json.Json


/**
 * Performs platform-specific initialization and pre-checks.
 *
 * ⚠️ Execution order is not guaranteed, and code that takes too long should not be executed.
 *
 */
expect fun init()

expect val logger: KLogger
val defaultJson: Json = Json {
    prettyPrint = true
    isLenient = false
    encodeDefaults = true
}