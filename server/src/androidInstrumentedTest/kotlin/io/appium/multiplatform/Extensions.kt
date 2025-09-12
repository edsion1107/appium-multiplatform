package io.appium.multiplatform


import io.appium.multiplatform.jvm.ProtobufContentConverter
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CompletableDeferred

val clientDefaultConfig: HttpClientConfig<*>.() -> Unit = {
    // 默认配置
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
    }
    install(HttpCookies)
    install(DefaultRequest) {
        url("http://localhost:8080")
        header(HttpHeaders.ContentType, ContentType.Application.ProtoBuf)
    }
    install(ContentNegotiation) {
        register(ContentType.Application.ProtoBuf, ProtobufContentConverter())
        json(defaultJson)
    }
    install(Resources)
    BrowserUserAgent()
}

suspend fun <T> withHttpClient(
    config: HttpClientConfig<*>.() -> Unit = clientDefaultConfig,
    block: suspend (HttpClient) -> T
): T {
    val deferred = CompletableDeferred<T>()
    val client = HttpClient(config)
    deferred.complete(block(client))
    return deferred.await()
}