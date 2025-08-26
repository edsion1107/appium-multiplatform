package io.appium.multiplatform.request

import io.ktor.resources.Resource

@Resource("/status")
class StatusRequest()

@Resource("/session/{sessionId}/element")
class ElementRequest(val sessionId: String)
