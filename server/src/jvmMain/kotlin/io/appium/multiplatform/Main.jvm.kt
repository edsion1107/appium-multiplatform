package io.appium.multiplatform

import io.appium.multiplatform.jvm.configSlf4j
import io.appium.multiplatform.service.ElementHandler
import io.appium.multiplatform.service.FakeBySelectorHandler
import io.appium.multiplatform.service.FindElementRequest
import io.appium.multiplatform.service.FindElementResponse
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.logger.SLF4JLogger

actual val logger: KLogger = run {
    configSlf4j()
    KotlinLogging.logger {}
}


actual fun init() {
    logger.debug { "init" }
    Thread.currentThread().contextClassLoader.getResource("application.yaml")
        ?.let { logger.info { "Starting application at $it" } }
}

val jvmModule = module {
    single(createdAtStart = true) {
        FakeBySelectorHandler(
            selectorType = FindElementRequest.SELECTOR_BY,
            elementType = FindElementResponse.ELEMENT_UIOBJECT2
        )
    }.bind<ElementHandler<*, *>>()
}

actual fun initKoin() = startKoin {
    logger(SLF4JLogger(level = Level.INFO))
    modules(
        commonModule, jvmModule
    )
}