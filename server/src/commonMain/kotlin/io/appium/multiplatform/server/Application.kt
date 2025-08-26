package io.appium.multiplatform.server

import io.appium.multiplatform.init
import io.appium.multiplatform.initKoin
import io.appium.multiplatform.server.routes.webdriverRoutes
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


@Suppress("unused")
private val static = object {
    init {
        init()
    }
}

fun main(args: Array<String>) {
    val koinApplication = initKoin()
    EngineMain.main(args)
    koinApplication.close()
}


fun Application.module() {
    log.info("host: ${environment.config.host}, port: ${environment.config.port}, startupMode: ${environment.startupMode}, startupTimeout: ${environment.startupTimeout}, classLoader: ${environment.classLoader}")
    configureResources()
    routing {
        get("/") {
            call.respondText("Hello, world!")
        }
        webdriverRoutes()
    }
//    grpc(){
//
//    }
}