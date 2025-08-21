package io.appium.multiplatform.protocol.webdriver

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Resource("/session/{sessionId}/element")
data class ElementRequest(val sessionId: String)

@Serializable
data class ElementBody(
    val strategy: String,
    val selector: String,
    val multiple: Boolean = false
)