package io.appium.multiplatform.server.route


import io.appium.multiplatform.jvm.StatusException.Companion.buildHttpStatusException
import io.appium.multiplatform.model.BySelector
import io.appium.multiplatform.model.UiObject
import io.appium.multiplatform.model.UiObject2
import io.appium.multiplatform.model.UiSelector
import io.appium.multiplatform.model.error.HttpStatusCode
import io.appium.multiplatform.request.SessionRequest
import io.appium.multiplatform.server.toHttpStatusException
import io.appium.multiplatform.service.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject


actual fun Route.configWebdriverRoute() {
    val logger = KotlinLogging.logger {}
    val byElementRepository: ElementRepository<BySelector, UiObject2> by inject(named(ElementRepositoryName.BY_SELECTOR))
    val uiElementRepository: ElementRepository<UiSelector, UiObject> by inject(named(ElementRepositoryName.UI_SELECTOR))

    post<SessionRequest.ElementRequest> {
        val req = runCatching {
            call.receive(FindElementRequest::class)
        }.onFailure {
            if (it is RequestValidationException) {
                call.respond(io.ktor.http.HttpStatusCode.BadRequest, it.toHttpStatusException().status)
                return@post
            }
            throw it
        }.onSuccess {
            if (it.isMultiple) {
                throw buildHttpStatusException(HttpStatusCode.HTTP_BAD_REQUEST, logger) {
                    put("reason", "isMultiple should be false")
                }
            }
        }.getOrThrow()
        val found = when (req.selectorCase) {
            FindElementRequest.SelectorCase.BY -> element { uiobject2 = byElementRepository.findElement(req.by) }
            FindElementRequest.SelectorCase.UI -> element { uiobject = uiElementRepository.findElement(req.ui) }
            FindElementRequest.SelectorCase.SELECTOR_NOT_SET -> {
                throw buildHttpStatusException(HttpStatusCode.HTTP_BAD_REQUEST, logger) {
                    put("reason", "SELECTOR_NOT_SET")
                }
            }
        }
        call.respond(findElementResponse { element = found })
    }
    post<SessionRequest.ElementsRequest> {
        val req = runCatching {
            call.receive(FindElementRequest::class)
        }.onFailure {
            if (it is RequestValidationException) {
                throw it.toHttpStatusException()
            }
            throw it
        }.onSuccess {
            if (!it.isMultiple) {
                throw buildHttpStatusException(HttpStatusCode.HTTP_BAD_REQUEST, logger) {
                    put("reason", "isMultiple should be true")
                }
            }
        }.getOrThrow()
        val found = when (req.selectorCase) {
            FindElementRequest.SelectorCase.BY -> byElementRepository.findElements(req.by)
                .map { element { uiobject2 = it } }

            FindElementRequest.SelectorCase.UI -> uiElementRepository.findElements(req.ui)
                .map { element { uiobject = it } }

            FindElementRequest.SelectorCase.SELECTOR_NOT_SET -> {
                throw buildHttpStatusException(HttpStatusCode.HTTP_BAD_REQUEST, logger) {
                    put("reason", "SELECTOR_NOT_SET")
                }
            }
        }
        call.respond(findElementsResponse { elements.addAll(found) })
    }
}

