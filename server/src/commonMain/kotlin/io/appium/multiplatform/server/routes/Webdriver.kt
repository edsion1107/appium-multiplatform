package io.appium.multiplatform.server.routes

import io.appium.multiplatform.models.buildInfo
import io.appium.multiplatform.protocol.webdriver.Status
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.webdriverRoutes() {
    get<Status> {
        call.respond(buildInfo)
    }
}
