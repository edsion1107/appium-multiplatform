package io.appium.multiplatform.server.route

import io.appium.multiplatform.koinExtension
import io.appium.multiplatform.model.*
import io.appium.multiplatform.model.error.HttpStatusCode
import io.appium.multiplatform.request.SessionRequest
import io.appium.multiplatform.service.*
import io.appium.multiplatform.withKtorClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.ktor.client.shouldBeBadRequest
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.mock.declareMock
import kotlin.uuid.Uuid

/**
 * @see io.ktor.server.routing.Route.configWebdriverRoute
 *
 * Note: When mocking Koin dependencies here, do not mock the implementation class directly using `mockk`.
 * Instead, use `declareMock`. Also, because the module declares multiple instances of the same interface with different names,
 * the `qualifier` (i.e., `named`) parameter must not be omitted.
 */
class WebdriverRouteTest : FunSpec(
), KoinTest {
    val logger = KotlinLogging.logger {}

    lateinit var bySelectorElementRepository: ElementRepository<BySelector, UiObject2>
    lateinit var uiSelectorElementRepository: ElementRepository<UiSelector, UiObject>

    init {
        extension(koinExtension)

        beforeEach {
            logger.info { "beforeContainer: ${it.name}" }
            bySelectorElementRepository =
                declareMock<ElementRepository<BySelector, UiObject2>>(named(ElementRepositoryName.BY_SELECTOR)) {
                    val mockUiObject2 = mockk<UiObject2>(relaxed = true)
                    every { findElement(any()) } returns mockUiObject2
                    every { findElements(any()) } returns listOf(mockUiObject2)
                }
            uiSelectorElementRepository =
                declareMock<ElementRepository<UiSelector, UiObject>>(named(ElementRepositoryName.UI_SELECTOR)) {
                    val mockUiObject = mockk<UiObject>(relaxed = true)
                    every { findElement(any()) } returns mockUiObject
                    every { findElements(any()) } returns listOf(mockUiObject)
                }
        }

        afterTest {
            logger.info { "afterContainer: ${it.a.name},${it.b.name}" }
            unmockkAll()
        }
        context("Positive Scenarios").config(enabled = true) {
            context("with BySelector") {
                test("ElementRequest") {
                    val protoBody = findElementRequest {
                        by = bySelector { clazz = "android.widget.TextView" }
                        isMultiple = false
                    }
                    val res = withKtorClient { client ->
                        client.post(SessionRequest.ElementRequest(sessionId = Uuid.random())) {
                            setBody(protoBody)
                        }
                    }
                    verify(exactly = 1) { bySelectorElementRepository.findElement(any<BySelector>()) }
                    res.shouldBeOK()
                    res.shouldHaveContentType(ContentType.Application.ProtoBuf)
                    val result = res.body<FindElementResponse>()
                    result.hasElement().shouldBeTrue()
                    result.element.hasUiobject2().shouldBeTrue()
                    result.element.hasUiobject().shouldBeFalse()
                    result.element.uiobject2.also {
                        it.shouldBeInstanceOf<UiObject2>()
                        it.className.shouldBeEmpty()
                    }
                }
                test("ElementsRequest") {
                    val protoBody = findElementRequest {
                        by = bySelector { clazz = "android.widget.TextView" }
                        isMultiple = true
                    }
                    val res = withKtorClient { client ->
                        client.post(SessionRequest.ElementsRequest(sessionId = Uuid.random())) {
                            setBody(protoBody)
                        }
                    }
                    verify(exactly = 1) { bySelectorElementRepository.findElements(any<BySelector>()) }
                    res.shouldBeOK()
                    res.shouldHaveContentType(ContentType.Application.ProtoBuf)
                    val result = res.body<FindElementsResponse>()
                    result.elementsList.shouldBeSingleton {
                        it.hasUiobject2().shouldBeTrue()
                        it.hasUiobject().shouldBeFalse()
                        it.uiobject2.shouldBeInstanceOf<UiObject2>()
                        it.uiobject2.className.shouldBeEmpty()
                    }
                }
            }
            context("with UiSelector") {
                test("ElementRequest") {
                    val protoBody = findElementRequest {
                        ui = uiSelector { className = "android.widget.TextView" }
                        isMultiple = false
                    }
                    val res = withKtorClient { client ->
                        client.post(SessionRequest.ElementRequest(sessionId = Uuid.random())) {
                            setBody(protoBody)
                        }
                    }
                    verify(exactly = 1) { uiSelectorElementRepository.findElement(any<UiSelector>()) }
                    res.shouldBeOK()
                    res.shouldHaveContentType(ContentType.Application.ProtoBuf)
                    val result = res.body<FindElementResponse>()
                    result.hasElement().shouldBeTrue()
                    result.element.hasUiobject2().shouldBeFalse()
                    result.element.hasUiobject().shouldBeTrue()
                    result.element.uiobject.also {
                        it.shouldBeInstanceOf<UiObject>()
                        it.className.shouldBeEmpty()
                    }
                }
                test("ElementsRequest") {
                    val protoBody = findElementRequest {
                        ui = uiSelector { className = "android.widget.TextView" }
                        isMultiple = true
                    }
                    val res = withKtorClient { client ->
                        client.post(SessionRequest.ElementsRequest(sessionId = Uuid.random())) {
                            setBody(protoBody)
                        }
                    }
                    verify(exactly = 1) { uiSelectorElementRepository.findElements(any<UiSelector>()) }
                    res.shouldBeOK()
                    res.shouldHaveContentType(ContentType.Application.ProtoBuf)
                    val result = res.body<FindElementsResponse>()
                    result.elementsList.shouldBeSingleton {
                        it.hasUiobject2().shouldBeFalse()
                        it.hasUiobject().shouldBeTrue()
                        it.uiobject.shouldBeInstanceOf<UiObject>()
                        it.uiobject.className.shouldBeEmpty()
                    }
                }
            }
        }
        context("Negative Scenarios").config(enabled = true) {
            context("ElementRequest RequestValidation") {
                test("body is empty") {
                    val protoBody = findElementRequest {}
                    val res = withKtorClient { client ->
                        client.post(SessionRequest.ElementRequest(sessionId = Uuid.random())) {
                            setBody(protoBody)
                        }
                    }
                    res.shouldBeBadRequest()
                    val result = res.body<Status>()
                    result.hasHttpStatusCode().shouldBeTrue()
                    result.hasRpcCode().shouldBeFalse()
                    result.hasRpcCode().shouldBeFalse()
                    result.httpStatusCode.shouldBe(HttpStatusCode.HTTP_BAD_REQUEST)
                    result.message.shouldContain("- selector: exactly one field is required in oneof [required]")
                }
                test("set is_multiple") {
                    val protoBody = findElementRequest {
                        by = bySelector { clazz = "android.widget.TextView" }
                        isMultiple = true
                    }
                    val res = withKtorClient { client ->
                        client.post(SessionRequest.ElementRequest(sessionId = Uuid.random())) {
                            setBody(protoBody)
                        }
                    }
                    res.shouldBeBadRequest()
                    val result = res.body<Status>()
                    result.hasHttpStatusCode().shouldBeTrue()
                    result.hasRpcCode().shouldBeFalse()
                    result.hasRpcCode().shouldBeFalse()
                    result.httpStatusCode.shouldBe(HttpStatusCode.HTTP_BAD_REQUEST)
                    result.message.shouldContain("isMultiple should be false")
                }
            }
            context("ElementsRequest RequestValidation") {
                test("body is empty") {
                    val protoBody = findElementRequest {}
                    val res = withKtorClient { client ->
                        client.post(SessionRequest.ElementsRequest(sessionId = Uuid.random())) {
                            setBody(protoBody)
                        }
                    }
                    res.shouldBeBadRequest()
                    val result = res.body<Status>()
                    result.hasHttpStatusCode().shouldBeTrue()
                    result.hasRpcCode().shouldBeFalse()
                    result.hasRpcCode().shouldBeFalse()
                    result.httpStatusCode.shouldBe(HttpStatusCode.HTTP_BAD_REQUEST)
                    result.message.shouldContain("- selector: exactly one field is required in oneof [required]")
                }
                test("forget is_multiple") {
                    val protoBody = findElementRequest {
                        by = bySelector { clazz = "android.widget.TextView" }
                        isMultiple = false
                    }
                    val res = withKtorClient { client ->
                        client.post(SessionRequest.ElementsRequest(sessionId = Uuid.random())) {
                            setBody(protoBody)
                        }
                    }
                    res.shouldBeBadRequest()
                    val result = res.body<Status>()
                    result.hasHttpStatusCode().shouldBeTrue()
                    result.hasRpcCode().shouldBeFalse()
                    result.hasRpcCode().shouldBeFalse()
                    result.httpStatusCode.shouldBe(HttpStatusCode.HTTP_BAD_REQUEST)
                    result.message.shouldContain("isMultiple should be true")
                }
            }
        }
    }
}