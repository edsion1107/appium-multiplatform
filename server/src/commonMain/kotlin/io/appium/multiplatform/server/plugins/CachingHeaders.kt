package io.appium.multiplatform.server.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*

fun Application.configureCachingHeaders() {
    install(CachingHeaders) {
    }
}