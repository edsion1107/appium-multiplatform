package io.appium.multiplatform

import io.appium.multiplatform.jvm.ProtobufContentConverter
import io.appium.multiplatform.service.UiDeviceProvider
import io.kotest.koin.KoinExtension
import io.kotest.koin.KoinLifecycleMode
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import org.koin.dsl.module

suspend fun <T> withKtorClient(
    clientConfig: HttpClientConfig<*>.() -> Unit = {
        // 默认配置
        install(Logging) { logger = Logger.DEFAULT; level = LogLevel.HEADERS }
        install(HttpCookies)
        install(DefaultRequest) { header(HttpHeaders.ContentType, ContentType.Application.ProtoBuf) }
        install(ContentNegotiation) {
            register(ContentType.Application.ProtoBuf, ProtobufContentConverter())
            json(defaultJson)
        }
        install(Resources)
        CurlUserAgent()
    }, block: suspend (HttpClient) -> T
): T {
    val deferred = CompletableDeferred<T>()
    testApplication {
        environment {
            config = ApplicationConfig("application.yaml")
        }
        val client = createClient(clientConfig)
        deferred.complete(block(client))
    }
    return deferred.await()
}

val koinExtension = KoinExtension(
    modules = listOf(androidModule, module {
        single<UiDeviceProvider> { mockk<UiDeviceProvider>(relaxed = true) }
    }),
    mode = KoinLifecycleMode.Test
)