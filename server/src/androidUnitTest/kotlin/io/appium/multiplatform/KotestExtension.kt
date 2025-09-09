package io.appium.multiplatform

import io.appium.multiplatform.jvm.ProtobufContentConverter
import io.appium.multiplatform.model.BySelector
import io.appium.multiplatform.model.UiObject
import io.appium.multiplatform.model.UiObject2
import io.appium.multiplatform.model.UiSelector
import io.appium.multiplatform.service.BySelectorElementRepositoryImpl
import io.appium.multiplatform.service.ElementRepository
import io.appium.multiplatform.service.ElementRepositoryName
import io.appium.multiplatform.service.UiSelectorElementRepositoryImpl
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
import io.mockk.mockkClass
import kotlinx.coroutines.CompletableDeferred
import org.koin.core.qualifier.named
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

/**
 *  @see androidModule
 */
val testAndroidModule = module {
    single<ElementRepository<BySelector, UiObject2>>(named(ElementRepositoryName.BY_SELECTOR)) {
        BySelectorElementRepositoryImpl(get())
    }

    single<ElementRepository<UiSelector, UiObject>>(named(ElementRepositoryName.UI_SELECTOR)) {
        UiSelectorElementRepositoryImpl(get())
    }
}
val koinExtension = KoinExtension(
    module = testAndroidModule,
    mockProvider = {
        mockkClass(it, relaxed = true, relaxUnitFun = true)
//        mockkClass(it)
    },
    mode = KoinLifecycleMode.Test
)
