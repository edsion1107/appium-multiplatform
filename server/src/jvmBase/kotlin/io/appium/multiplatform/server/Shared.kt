package io.appium.multiplatform.server

import io.appium.multiplatform.defaultJson
import io.appium.multiplatform.jvm.ProtobufContentConverter
import io.appium.multiplatform.jvm.StatusException
import io.appium.multiplatform.jvm.StatusException.Companion.buildHttpStatusException
import io.appium.multiplatform.jvm.StatusException.Companion.toHttpStatusCode
import io.appium.multiplatform.jvm.pbValidator
import io.appium.multiplatform.service.FindElementRequest
import io.appium.multiplatform.service.FindElementResponse
import io.appium.multiplatform.service.FindElementsResponse
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.util.reflect.*

fun build.buf.protovalidate.ValidationResult.toRequestValidationResult(): ValidationResult {
    return if (this.isSuccess) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid(this.toString())
    }
}

fun RequestValidationException.toHttpStatusException(logger: KLogger? = null): StatusException {
    return buildHttpStatusException(io.appium.multiplatform.model.error.HttpStatusCode.HTTP_BAD_REQUEST, logger) {
        put("reasons", this@toHttpStatusException.message)
    }
}

actual fun requestValidation(): RequestValidationConfig.() -> Unit = {
    validate<FindElementRequest> { pbValidator.validate(it).toRequestValidationResult() }
    validate<FindElementResponse> { pbValidator.validate(it).toRequestValidationResult() }
    validate<FindElementsResponse> { pbValidator.validate(it).toRequestValidationResult() }
}

actual fun contentNegotiation(): ContentNegotiationConfig.() -> Unit = {
    checkAcceptHeaderCompliance = true
    register(ContentType.Application.ProtoBuf, ProtobufContentConverter())
    json(defaultJson)
}

actual fun statusPagesConfig(): StatusPagesConfig.() -> Unit = {
    exception<StatusException> { call, cause ->
        with(cause.status) {
            val httpStatusCode = when {
                hasHttpStatusCode() -> HttpStatusCode.fromValue(httpStatusCode.number)
                hasWebdriverErrorCode() -> HttpStatusCode.fromValue(webdriverErrorCode.toHttpStatusCode().number)
                hasRpcCode() -> HttpStatusCode.OK
                else -> HttpStatusCode.NotImplemented
            }
            call.respond(status = httpStatusCode, message = this, messageType = TypeInfo(this::class))
        }
    }
}