package io.appium.multiplatform.server.routes

import com.squareup.wire.OneOf
import io.appium.multiplatform.logger
import io.appium.multiplatform.model.BySelector
import io.appium.multiplatform.model.state
import io.appium.multiplatform.protocol.webdriver.ElementBody
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
import org.koin.ktor.ext.inject


fun Routing.webdriverRoutes() {
    val webdriverServiceServer: WebdriverServiceServer by inject()
    get<StatusRequest> { req ->
        val selector = BySelector(clazz = "android.widget.TextView")
        val proto = FindElementRequest(selector = OneOf(FindElementRequest.SELECTOR_BY, selector))
        val res = webdriverServiceServer.FindElement(proto)
        logger.info { res }
        call.respond(state)
    }
    get<ElementRequest> { req ->
        logger.info { req }
        val request = call.receive<FindElementRequest>()
        logger.info { request }
        val res = webdriverServiceServer.FindElement(request)
        call.respond(res)
    }

    post<ElementRequest> { req ->

        val body = call.receive<ElementBody>()
        ContentType.Application.ProtoBuf
        ContentType.Text
        call.respondText("post element :$body", ContentType.Text.Plain, HttpStatusCode.OK)
    }
}
