package io.appium.multiplatform.server.plugins


import io.appium.multiplatform.defaultJson
import io.appium.multiplatform.model.ProtobufConverter

import io.ktor.http.*

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.resources.*



suspend fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(defaultJson)
        register(ContentType.Application.ProtoBuf, ProtobufConverter())
    }
}

suspend fun Application.configureRequestValidation() {
    install(RequestValidation) {
    }
}

suspend fun Application.configureResources() {
    install(Resources)
}

suspend fun Application.configureCallLogging() {
    install(CallLogging) {
        disableDefaultColors() // jansi not support android .
//        level = Level.INFO // ⚠️not config global slf4j log level
//        logger = KtorSimpleLogger(io.appium.multiplatform.logger.name)
        // The framework and plugin use slf4j: Application.log(environment.log),
        // and the business code uses KotlinLogging: io.appium.multiplatform.logger
        // sample: log.toKLogger().info { "Hello World" } or io.appium.multiplatform.logger.debug { "Hello World" }
    }
}

suspend fun Application.requiredPlugins() {
    configureContentNegotiation()
    configureRequestValidation()
    configureResources()
    configureCallLogging()
}