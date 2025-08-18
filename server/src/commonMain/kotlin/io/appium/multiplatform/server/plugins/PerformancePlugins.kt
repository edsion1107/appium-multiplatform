package io.appium.multiplatform.server.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.partialcontent.*

fun Application.configureCachingHeaders() {
    install(CachingHeaders) {
    }
}

fun Application.configureCompression() {
    install(Compression) {
        deflate()
        gzip()
    }
}

fun Application.configureConditionalHeaders() {
    install(ConditionalHeaders) {
    }
}

fun Application.configurePartialContent() {
    install(PartialContent) {
    }
}

@Suppress("unused")
fun Application.performancePlugins() {
    configureCachingHeaders()
    configureCompression()
    configureConditionalHeaders()
    configurePartialContent()
}