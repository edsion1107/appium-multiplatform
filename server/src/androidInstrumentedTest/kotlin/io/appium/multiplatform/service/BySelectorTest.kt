package io.appium.multiplatform.service

//import io.kotest.assertions.assertSoftly
//import io.kotest.matchers.should
//import io.kotest.matchers.shouldBe


import io.appium.multiplatform.model.bySelector
import io.appium.multiplatform.request.SessionRequest
import io.appium.multiplatform.withHttpClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.uuid.Uuid

class BySelectorTest {
    val logger = KotlinLogging.logger {}

    @Test
    fun testFindElement(): Unit = runBlocking {
        val reqBody = findElementRequest {
            by = bySelector {
                clazz = "android.widget.TextView"
            }
            isMultiple = false
        }
        val res = withHttpClient { client ->
            client.post(SessionRequest.ElementRequest(sessionId = Uuid.random())) {
                setBody(reqBody)
            }
        }
        logger.info { "res: $res" }

        assertSoftly {
            res.shouldBeOK()
            res.shouldHaveContentType(ContentType.Application.ProtoBuf)
            val result = res.body<FindElementResponse>()

            logger.info { "result: $result, uiobject2: ${result.element}" }
            result.should {
                it.hasElement().shouldBeTrue()
                it.element.hasUiobject().shouldBeFalse()
                it.element.hasUiobject2().shouldBeTrue()
                it.element.uiobject2.className.shouldBe(reqBody.by.clazz)
            }
        }
    }

    @Test
    fun testFindElements(): Unit = runBlocking {
        val reqBody = findElementRequest {
            by = bySelector {
                clazz = "android.widget.TextView"
            }
            isMultiple = true
        }
        val res = withHttpClient { client ->
            client.post(SessionRequest.ElementsRequest(sessionId = Uuid.random())) {
                setBody(reqBody)
            }
        }
        logger.info { "res: $res" }

        assertSoftly {
            res.shouldBeOK()
            res.shouldHaveContentType(ContentType.Application.ProtoBuf)
            val result = res.body<FindElementsResponse>()

            logger.info { "result: $result, uiobject2: ${result.elementsList}" }
            result.should {
                it.elementsList.shouldNotBeEmpty()
                it.elementsList.forEach { element ->
                    element.hasUiobject().shouldBeFalse()
                    element.hasUiobject2().shouldBeTrue()
                    element.uiobject2.className.shouldBe(reqBody.by.clazz)
                }
            }
        }
    }
}