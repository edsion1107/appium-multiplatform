package io.appium.multiplatform.service


import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import io.appium.multiplatform.koinExtension
import io.appium.multiplatform.model.Status
import io.appium.multiplatform.model.bySelector
import io.appium.multiplatform.model.error.WebDriverErrorCode
import io.appium.multiplatform.request.SessionRequest
import io.appium.multiplatform.withKtorClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.ktor.client.shouldBeNotFound
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.koin.test.KoinTest
import org.koin.test.mock.declareMock
import kotlin.uuid.Uuid


/**
 * @see BySelectorElementRepositoryImpl
 */
class BySelectorElementRepositoryImplTest : FunSpec(), KoinTest {
    val logger = KotlinLogging.logger {}

    lateinit var mockUiDevice: UiDevice

    init {
        extension(koinExtension)

        context("Positive Scenarios").config(enabled = true) {
            beforeTest {
                val uiDeviceProvider = declareMock<UiDeviceProvider>()
                mockUiDevice = uiDeviceProvider.get()
                // Must keep using the same mockUiDevice instance to verify a single invocation;
                // otherwise, a new object would be created each time.
                every { uiDeviceProvider.get(any()) } returns mockUiDevice
                val mockUiObject2 = mockk<UiObject2>(relaxed = true)
                every { mockUiDevice.findObject(any<BySelector>()) } returns mockUiObject2
                every { mockUiDevice.findObjects(any<BySelector>()) } returns listOf(mockUiObject2)
            }
            test("findElement") {
                val protoBody = findElementRequest {
                    by = bySelector {
                        clazz = "android.widget.TextView"
                    }
                    isMultiple = false
                }
                val res = withKtorClient { client ->
                    client.post(SessionRequest.ElementRequest(sessionId = Uuid.random())) {
                        setBody(protoBody)
                    }
                }

                logger.info { "res: $res" }
                verify(atLeast = 1, atMost = 1) { mockUiDevice.findObject(any<BySelector>()) }
                res.shouldBeOK()
                res.shouldHaveContentType(ContentType.Application.ProtoBuf)
                val result = res.body<FindElementResponse>()
                result.hasElement().shouldBeTrue()
                result.element.hasUiobject2().shouldBeTrue()
                result.element.hasUiobject().shouldBeFalse()
                result.element.uiobject2.also {
                    it.shouldBeInstanceOf<io.appium.multiplatform.model.UiObject2>()
                    // className should be empty and not match request, because this is a fake UiObject2
                    it.className.shouldBeEmpty()
                    it.className.shouldNotBe(protoBody.by.clazz)
                }
            }
            test("findElements") {
                val protoBody = findElementRequest {
                    by = bySelector {
                        clazz = "android.widget.TextView"
                    }
                    isMultiple = true
                }
                val res = withKtorClient { client ->
                    client.post(SessionRequest.ElementsRequest(sessionId = Uuid.random())) {
                        setBody(protoBody)
                    }
                }
                logger.info { "res: $res" }
                verify(atLeast = 1, atMost = 1) { mockUiDevice.findObjects(any<BySelector>()) }
                res.shouldBeOK()
                res.shouldHaveContentType(ContentType.Application.ProtoBuf)
                val result = res.body<FindElementsResponse>()
                result.elementsList.shouldBeSingleton {
                    it.hasUiobject2().shouldBeTrue()
                    it.hasUiobject().shouldBeFalse()
                    it.uiobject2.shouldBeInstanceOf<io.appium.multiplatform.model.UiObject2>()
                    it.uiobject2.className.shouldBeEmpty()
                    it.uiobject2.className.shouldNotBe(protoBody.by.clazz)
                }
            }
        }
        context("Negative Scenarios").config(enabled = true) {
            beforeEach {
                val uiDeviceProvider = declareMock<UiDeviceProvider>()
                mockUiDevice = uiDeviceProvider.get()
                // Must keep using the same mockUiDevice instance to verify a single invocation;
                // otherwise, a new object would be created each time.
                every { uiDeviceProvider.get(any()) } returns mockUiDevice
                every { mockUiDevice.findObject(any<BySelector>()) } returns null
                every { mockUiDevice.findObjects(any<BySelector>()) } returns emptyList()
            }
            test("findElement") {
                val protoBody = findElementRequest {
                    by = bySelector {
                        clazz = "android.widget.TextView"
                    }
                    isMultiple = false
                }
                val res = withKtorClient { client ->
                    client.post(SessionRequest.ElementRequest(sessionId = Uuid.random())) {
                        setBody(protoBody)
                    }
                }

                logger.info { "res: $res" }
                verify(atLeast = 1, atMost = 1) { mockUiDevice.findObject(any<BySelector>()) }
                res.shouldBeNotFound()
                res.shouldHaveContentType(ContentType.Application.ProtoBuf)
                val result = res.body<Status>()
                result.hasWebdriverErrorCode().shouldBeTrue()
                result.hasHttpStatusCode().shouldBeFalse()
                result.hasRpcCode().shouldBeFalse()
                result.webdriverErrorCode.shouldBe(WebDriverErrorCode.WD_NO_SUCH_ELEMENT)

            }
            test("findElements") {
                val protoBody = findElementRequest {
                    by = bySelector {
                        clazz = "android.widget.TextView"
                    }
                    isMultiple = true
                }
                val res = withKtorClient { client ->
                    client.post(SessionRequest.ElementsRequest(sessionId = Uuid.random())) {
                        setBody(protoBody)
                    }
                }
                logger.info { "res: $res" }
                verify(atLeast = 1, atMost = 1) { mockUiDevice.findObjects(any<BySelector>()) }
                res.shouldBeNotFound()
                res.shouldHaveContentType(ContentType.Application.ProtoBuf)
                val result = res.body<Status>()
                result.hasWebdriverErrorCode().shouldBeTrue()
                result.hasHttpStatusCode().shouldBeFalse()
                result.hasRpcCode().shouldBeFalse()
                result.webdriverErrorCode.shouldBe(WebDriverErrorCode.WD_NO_SUCH_ELEMENT)
            }
        }
    }

}
