package io.appium.multiplatform.server.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*

fun Application.configureCompression() {
    install(Compression) {
        deflate()
        gzip()
    }
}