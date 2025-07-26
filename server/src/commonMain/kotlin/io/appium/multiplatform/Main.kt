package io.appium.multiplatform

import io.github.oshai.kotlinlogging.KLogger
import kotlinx.serialization.json.Json


/**
 * Performs platform-specific initialization and pre-checks.
 *
 * ⚠️ Due to the uncertain initialization order of top-level properties,
 * avoid directly initializing top-level values that depend on init logic.
 * Instead, prefer using `by lazy` to ensure deferred and safe initialization.
 *
 */
expect fun init()

expect val logger: KLogger
expect val defaultJson: Json