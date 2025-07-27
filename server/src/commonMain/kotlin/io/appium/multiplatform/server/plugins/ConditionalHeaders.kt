package io.appium.multiplatform.server.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.conditionalheaders.*

fun Application.configureConditionalHeaders() {
    install(ConditionalHeaders) {
    }
}