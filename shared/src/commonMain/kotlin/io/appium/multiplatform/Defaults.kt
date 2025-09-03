package io.appium.multiplatform

import kotlinx.serialization.json.Json

val defaultJson: Json = Json {
    isLenient = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    useArrayPolymorphism = false
    prettyPrint = true
    encodeDefaults = true
}
