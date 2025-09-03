package io.appium.multiplatform.client


import io.appium.multiplatform.model.Status
import io.appium.multiplatform.model.bySelector
import io.appium.multiplatform.model.error.HttpStatusCode
import io.appium.multiplatform.request.SessionRequest
import io.appium.multiplatform.service.FindElementResponse
import io.appium.multiplatform.service.FindElementsResponse
import io.appium.multiplatform.service.findElementRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.ktor.client.shouldBeBadRequest
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.uuid.Uuid

class TestWebdriver : FunSpec(
) {
    val logger = KotlinLogging.logger {}


    override suspend fun beforeSpec(spec: Spec) {
        logger.info { "beforeSpec: $spec" }
    }

    suspend fun HttpResponse.toStatus(): Status {
        require(this.status != io.ktor.http.HttpStatusCode.OK)
        return this.body<Status>()
    }

    init {
//        extensions(KtorClientExtension())
        test("findElement with bySelector") {
            val reqBody = findElementRequest {
                by = bySelector {
                    clazz = "android.widget.TextView"
                }
            }
            logger.info { "req: $reqBody" }
            val res = httpClient.post(SessionRequest.ElementRequest(sessionId = Uuid.random())) {
                setBody(reqBody)
            }

            logger.info { "res: $res" }
            assertSoftly {
                res.shouldBeOK()
                res.shouldHaveContentType(ContentType.Application.ProtoBuf)
                val result = res.body<FindElementResponse>()

                logger.info { "result: $result, uiobject2: ${result.element}" }
                result.should {
                    it.element.uiobject2.className shouldBe reqBody.by.clazz
                }
            }
        }

        test("findElements with bySelector") {
            val reqBody = findElementRequest {
                by = bySelector {
                    clazz = "android.widget.TextView"
                }
                isMultiple = true
            }

            logger.info { "req: $reqBody" }
            val res = httpClient.post(SessionRequest.ElementsRequest(sessionId = Uuid.random())) {
                setBody(reqBody)
            }

            logger.info { "res: $res" }
            assertSoftly {
                res.shouldBeOK()
                res.shouldHaveContentType(ContentType.Application.ProtoBuf)

                val result = res.body<FindElementsResponse>()
                logger.info { "result: $result, elementsList: ${result.elementsList}" }
                result.elementsCount.shouldBeGreaterThan(0) //JVM上只有
            }
        }
        test("findElement without selector") {
            val reqBody = findElementRequest { }
            logger.info { "req: $reqBody" }
            val res = httpClient.post(SessionRequest.ElementRequest(sessionId = Uuid.random())) {
                setBody(reqBody)
            }

            logger.info { "res: $res" }
            assertSoftly {
                res.shouldBeBadRequest()
                res.shouldHaveContentType(ContentType.Application.ProtoBuf)
                val result = res.toStatus()
                logger.info { "result: $result" }
                result.httpStatusCode.shouldBe(HttpStatusCode.HTTP_BAD_REQUEST)
                result.message.shouldNotBeBlank()
            }
        }
    }
}
