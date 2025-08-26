package io.appium.multiplatform.client

import io.appium.multiplatform.jvm.ProtobufContentConverter
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.Spec
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*


val httpClient by lazy {
    HttpClient {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
        install(DefaultRequest) {
            url("http://localhost:8080")
            header(HttpHeaders.ContentType, ContentType.Application.ProtoBuf)
        }
        install(ContentNegotiation) {
            register(ContentType.Application.ProtoBuf, ProtobufContentConverter())
            json()
        }
        install(Resources)
        CurlUserAgent()
    }
}

class KtorClientExtension : BeforeSpecListener, AfterSpecListener {
    override suspend fun beforeSpec(spec: Spec) {
        httpClient.head("/")
    }

    override suspend fun afterSpec(spec: Spec) {
        httpClient.close()
    }
}
