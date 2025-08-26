package io.appium.multiplatform.server.routes

import com.squareup.wire.OneOf
import io.appium.multiplatform.logger
import io.appium.multiplatform.model.BySelector
import io.appium.multiplatform.model.state
import io.appium.multiplatform.protocol.webdriver.ElementRequest
import io.appium.multiplatform.protocol.webdriver.StatusRequest
import io.appium.multiplatform.service.FindElementRequest
import io.appium.multiplatform.service.WebdriverServiceServer
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.typeInfo
import io.ktor.utils.io.*
import org.koin.ktor.ext.inject


@OptIn(InternalAPI::class)
fun Routing.webdriverRoutes() {
    val webdriverServiceServer: WebdriverServiceServer by inject()
    get<StatusRequest> { req ->
        call.respond(state, typeInfo<io.appium.multiplatform.model.State>())
    }
    post<ElementRequest> { req ->
        if (call.request.contentType() == ContentType.Application.ProtoBuf) {
            val request = call.receive<FindElementRequest>()
            val res = webdriverServiceServer.FindElement(request)
            call.respond(res)
        }
    }
}
