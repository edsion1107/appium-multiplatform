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
    }
}


