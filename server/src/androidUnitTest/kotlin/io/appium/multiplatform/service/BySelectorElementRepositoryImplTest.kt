package io.appium.multiplatform.service

import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import io.appium.multiplatform.koinExtension
import io.appium.multiplatform.model.bySelector
import io.appium.multiplatform.request.SessionRequest
import io.appium.multiplatform.withKtorClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import kotlin.uuid.Uuid


/**
 * @see BySelectorElementRepositoryImpl
 */
class BySelectorElementRepositoryImplTest : FunSpec() {
    val logger = KotlinLogging.logger {}

    init {
        extension(koinExtension)
        beforeTest {
            val mockUiObject2 = mockk<UiObject2>()
            val mockUiDevice = mockk<UiDevice>()
            every { mockUiDevice.findObject(any<BySelector>()) } returns mockUiObject2
            every { mockUiDevice.findObjects(any<BySelector>()) } returns listOf(mockUiObject2, mockUiObject2)
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
            res.shouldBeOK()
            res.shouldHaveContentType(ContentType.Application.ProtoBuf)
            val result = res.body<FindElementResponse>()
            result.element.shouldBeInstanceOf<Element>()
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
            res.shouldBeOK()
            res.shouldHaveContentType(ContentType.Application.ProtoBuf)
            val result = res.body<FindElementsResponse>()
            result.elementsList.shouldBeInstanceOf<List<Element>>()
//            result.elementsList.hasUiobject2().shouldBeTrue()
//            result.elementsList.hasUiobject().shouldBeFalse()
//            result.elementsList.uiobject2.also {
//                it.shouldBeInstanceOf<io.appium.multiplatform.model.UiObject2>()
//                // className should be empty and not match request, because this is a fake UiObject2
//                it.className.shouldBeEmpty()
//                it.className.shouldNotBe(protoBody.by.clazz)
//            }
        }
    }

}
