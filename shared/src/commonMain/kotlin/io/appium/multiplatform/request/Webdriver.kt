@file:OptIn(ExperimentalUuidApi::class)

/**
 * Type-safe routing for the WebDriver protocol.
 *
 * This approach provides advantages for modularization and organizing complex nested routes,
 * while also preventing common mistakes such as duplicate path registrations.
 *
 * Be cautious when using this:
 * - Nested classes must correctly declare their parent.
 * - Using [io.ktor.server.routing.post] instead of [io.ktor.server.resources.post] will cause errors.
 */
package io.appium.multiplatform.request


import io.ktor.resources.*
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Resource("/status")
class StatusRequest()


@Serializable
@Resource("/session")
class SessionRequest() {
    @Serializable
    @Resource("/{sessionId}/element")
    class ElementRequest(val parent: SessionRequest = SessionRequest(), val sessionId: Uuid)

    @Serializable
    @Resource("/{sessionId}/elements")
    class ElementsRequest(val parent: SessionRequest = SessionRequest(), val sessionId: Uuid)
}

@Serializable
data class WebdriverSession(val sessionId: Uuid, val http: Boolean)


