package io.appium.multiplatform.model

import com.squareup.wire.OneOf
import io.appium.multiplatform.model.error.WebDriverErrorCode
import io.github.oshai.kotlinlogging.KLogger

class StatusException(
    val status: Status,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: status.message, cause) {
    companion object {
        fun Status.toException(
            logger: KLogger? = io.appium.multiplatform.logger,
            args: Map<String, Any?>? = emptyMap()
        ) = StatusException(this@toException).also {
            logger?.atError {
                this.message = this@toException.message
                this.cause = it
                this.payload = args
            }
        }


        fun buildWebdriverException(
            errorCode: WebDriverErrorCode,
            logger: KLogger? = io.appium.multiplatform.logger,
            args: MutableMap<String, Any?>.() -> Unit = {}
        ): StatusException {
            val payload: Map<String, Any?> = buildMap(args)
            return Status(
                code = OneOf(Status.CODE_WEBDRIVER_ERROR_CODE, errorCode),
                message = buildString {
                    append(errorCode.name)
                    append(payload.entries.joinToString(",", prefix = ", [Context] "))
                }
            ).toException(logger, payload)
        }

    }
}


