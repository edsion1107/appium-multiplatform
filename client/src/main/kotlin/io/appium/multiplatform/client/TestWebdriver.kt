package io.appium.multiplatform.client


import io.appium.multiplatform.model.bySelector
import io.appium.multiplatform.request.ElementRequest
import io.appium.multiplatform.request.StatusRequest
import io.appium.multiplatform.service.FindElementResponse
import io.appium.multiplatform.service.findElementRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*


class TestWebdriver : FunSpec(
) {
    val logger = KotlinLogging.logger {}


    override suspend fun beforeSpec(spec: Spec) {
        logger.info { "beforeSpec: $spec" }
    }

    init {
        extensions(KtorClientExtension())
        test("findElement") {
            val reqBody = findElementRequest {
                by = bySelector {
                    clazz = "android.widget.TextView"
                }
            }

            logger.info { "req: $reqBody" }
            val res = httpClient.post(ElementRequest(sessionId = "sid")) {
                setBody(reqBody)
            }

            logger.info { "res: $res" }
            res.request.headers.forEach { key, values ->
                logger.info { "request headers: $key=$values" }
            }
            res.headers.forEach { key, values ->
                logger.info { "response headers: $key=$values" }
            }
            res.shouldBeOK()
            res.shouldHaveContentType(ContentType.Application.ProtoBuf)


            val result = res.body<FindElementResponse>()
            logger.info { "result: $result, uiobject2: ${result.uiobject2}" }
            result.should {
                it.uiobject2.className shouldBe reqBody.by.clazz
            }
        }
        test("state") {
            val res = httpClient.get(StatusRequest())
            res.shouldBeOK()
            res.shouldHaveContentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
            val text = res.bodyAsText()
            logger.info { "status: $text" }
        }
    }
}
