package io.appium.multiplatform


import io.appium.multiplatform.jvm.configSlf4j
import io.appium.multiplatform.jvm.setSystemPropertyIfAbsent
import io.appium.multiplatform.service.*
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.bind
import org.koin.dsl.module

actual val logger: KLogger = run {
    setSystemPropertyIfAbsent("kotlin-logging-to-android-native", "true")
    configSlf4j()
    KotlinLogging.logger {}
    // set loglevel for KLogger(Android logcat):
    // `adb shell setprop log.tag.io.appium.multiplatform.Main_android VERBOSE`
}

actual fun init() {
    logger.debug { "init" }
    Thread.currentThread().contextClassLoader?.getResource("application.yaml")
        ?.let { logger.info { "Starting application at $it" } }
}

val androidModule = module {
    single(createdAtStart = true) {
        BySelectorHandler(
            uiDevice = UiDeviceProvider.get(),
            selectorType = FindElementRequest.SELECTOR_BY,
            elementType = FindElementResponse.ELEMENT_UIOBJECT2
        )
    }.bind<ElementHandler<*, *>>()
}

actual fun initKoin() = startKoin {
    logger(AndroidLogger(level = Level.INFO))
    modules(
        commonModule, androidModule
    )
}