package io.appium.multiplatform.jvm

import io.appium.multiplatform.model.Status
import io.appium.multiplatform.model.error.HttpStatusCode
import io.appium.multiplatform.model.error.RpcCode
import io.appium.multiplatform.model.error.WebDriverErrorCode
import io.appium.multiplatform.model.status
import io.github.oshai.kotlinlogging.KLogger

/**
 * 由[Status]定义的通用异常。
 *
 * client 与 server 之间传输异常时涉及序列化（跨编程语言）和缺少上下文，这里通过 Status（protobuf）解决。
 * 这样还有一个好处就是始终只有StatusException一个受检异常，在ktor-server-status-pages插件中处理即可。
 */
class StatusException(
    val status: Status,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: status.message, cause) {
    companion object {
        fun Status.toException(
            logger: KLogger? = null,
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
            logger: KLogger? = null,
            args: MutableMap<String, Any?>.() -> Unit = {}
        ): StatusException {
            val payload: Map<String, Any?> = buildMap(args)
            return status {
                webdriverErrorCode = errorCode
                message = buildString {
                    append(errorCode.name)
                    append(payload.entries.joinToString(",", prefix = ", [Context] "))
                }
            }.toException(logger, payload)
        }

        fun buildHttpStatusException(
            errorCode: HttpStatusCode,
            logger: KLogger? = null,
            args: MutableMap<String, Any?>.() -> Unit = {}
        ): StatusException {
            val payload: Map<String, Any?> = buildMap(args)
            return status {
                httpStatusCode = errorCode
                message = buildString {
                    append(errorCode.name)
                    append(payload.entries.joinToString(",", prefix = ", [Context] "))
                }
            }.toException(logger, payload)
        }

        fun buildRpcException(
            errorCode: RpcCode,
            logger: KLogger? = null,
            args: MutableMap<String, Any?>.() -> Unit = {}
        ): StatusException {
            val payload: Map<String, Any?> = buildMap(args)
            return status {
                rpcCode = errorCode
                message = buildString {
                    append(errorCode.name)
                    append(payload.entries.joinToString(",", prefix = ", [Context] "))
                }
            }.toException(logger, payload)
        }
        fun WebDriverErrorCode.toHttpStatusCode():HttpStatusCode{
            return when (this) {
                WebDriverErrorCode.WD_SUCCESS -> HttpStatusCode.HTTP_OK
                WebDriverErrorCode.WD_NO_ERROR -> HttpStatusCode.HTTP_OK
                WebDriverErrorCode.WD_ELEMENT_NOT_VISIBLE -> HttpStatusCode.HTTP_BAD_REQUEST
                WebDriverErrorCode.WD_INVALID_COOKIE_DOMAIN_OLD -> HttpStatusCode.HTTP_BAD_REQUEST
                WebDriverErrorCode.WD_INVALID_SELECTOR_OLD -> HttpStatusCode.HTTP_BAD_REQUEST
                WebDriverErrorCode.WD_NO_SUCH_ELEMENT_OLD -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_NO_SUCH_FRAME_OLD -> HttpStatusCode.HTTP_NOT_FOUND

                WebDriverErrorCode.WD_ELEMENT_CLICK_INTERCEPTED -> HttpStatusCode.HTTP_BAD_REQUEST
                WebDriverErrorCode.WD_ELEMENT_NOT_INTERACTABLE -> HttpStatusCode.HTTP_BAD_REQUEST
                WebDriverErrorCode.WD_INSECURE_CERTIFICATE -> HttpStatusCode.HTTP_BAD_REQUEST
                WebDriverErrorCode.WD_INVALID_ARGUMENT -> HttpStatusCode.HTTP_BAD_REQUEST
                WebDriverErrorCode.WD_INVALID_COOKIE_DOMAIN -> HttpStatusCode.HTTP_BAD_REQUEST
                WebDriverErrorCode.WD_INVALID_ELEMENT_STATE -> HttpStatusCode.HTTP_BAD_REQUEST
                WebDriverErrorCode.WD_INVALID_SELECTOR -> HttpStatusCode.HTTP_BAD_REQUEST
                WebDriverErrorCode.WD_INVALID_SESSION_ID -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_JAVASCRIPT_ERROR -> HttpStatusCode.HTTP_INTERNAL_SERVER_ERROR
                WebDriverErrorCode.WD_MOVE_TARGET_OUT_OF_BOUNDS -> HttpStatusCode.HTTP_INTERNAL_SERVER_ERROR
                WebDriverErrorCode.WD_NO_SUCH_ALERT -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_NO_SUCH_COOKIE -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_NO_SUCH_ELEMENT -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_NO_SUCH_FRAME -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_NO_SUCH_WINDOW -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_NO_SUCH_SHADOW_ROOT -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_SCRIPT_TIMEOUT -> HttpStatusCode.HTTP_INTERNAL_SERVER_ERROR
                WebDriverErrorCode.WD_SESSION_NOT_CREATED -> HttpStatusCode.HTTP_INTERNAL_SERVER_ERROR
                WebDriverErrorCode.WD_STALE_ELEMENT_REFERENCE -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_DETACHED_SHADOW_ROOT -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_TIMEOUT -> HttpStatusCode.HTTP_INTERNAL_SERVER_ERROR
                WebDriverErrorCode.WD_UNABLE_TO_SET_COOKIE -> HttpStatusCode.HTTP_INTERNAL_SERVER_ERROR
                WebDriverErrorCode.WD_UNABLE_TO_CAPTURE_SCREEN -> HttpStatusCode.HTTP_INTERNAL_SERVER_ERROR
                WebDriverErrorCode.WD_UNEXPECTED_ALERT_OPEN -> HttpStatusCode.HTTP_INTERNAL_SERVER_ERROR
                WebDriverErrorCode.WD_UNKNOWN_COMMAND -> HttpStatusCode.HTTP_NOT_FOUND
                WebDriverErrorCode.WD_UNKNOWN_ERROR -> HttpStatusCode.HTTP_INTERNAL_SERVER_ERROR
                WebDriverErrorCode.WD_UNKNOWN_METHOD -> HttpStatusCode.HTTP_METHOD_NOT_ALLOWED
                WebDriverErrorCode.WD_UNSUPPORTED_OPERATION -> HttpStatusCode.HTTP_INTERNAL_SERVER_ERROR

                else -> HttpStatusCode.HTTP_NOT_IMPLEMENTED
            }
        }
    }
}


