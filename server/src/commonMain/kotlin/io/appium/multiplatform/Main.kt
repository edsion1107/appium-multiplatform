package io.appium.multiplatform

import io.appium.multiplatform.models.AndroidExtraInfo
import io.appium.multiplatform.models.ExtraInfo
import io.github.oshai.kotlinlogging.KLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


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
    serializersModule = SerializersModule {
        polymorphic(ExtraInfo::class) {
            subclass(AndroidExtraInfo::class)
        }
//        contextual(Instant::class, InstantSerializer)
    }
}