package io.appium.multiplatform

import io.appium.multiplatform.jvm.setSystemPropertyIfAbsent
import io.appium.multiplatform.model.BySelector
import io.appium.multiplatform.model.UiObject
import io.appium.multiplatform.model.UiObject2
import io.appium.multiplatform.model.UiSelector
import io.appium.multiplatform.service.*
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val logger: KLogger = run {
    setSystemPropertyIfAbsent("kotlin-logging-to-android-native", "true")
    setSystemPropertyIfAbsent("org.slf4j.simpleLogger.logFile", "System.out")
    setSystemPropertyIfAbsent("org.slf4j.simpleLogger.defaultLogLevel", "INFO")
    KotlinLogging.logger {}
    // set loglevel for KLogger(Android logcat):
    // `adb shell setprop log.tag.io.appium.multiplatform.Main_android VERBOSE`
}

actual fun init() {
    println("init, ${logger.name}")
    Thread.currentThread().contextClassLoader?.getResource("application.yaml")
        ?.let { logger.info { "Starting application at $it" } }
}

val androidModule = module {
    single<UiDeviceProvider> { UiDeviceProvider() }
    single<ElementRepository<BySelector, UiObject2>>(named(ElementRepositoryName.BY_SELECTOR)) {
        BySelectorElementRepositoryImpl(get())
    }

    single<ElementRepository<UiSelector, UiObject>>(named(ElementRepositoryName.UI_SELECTOR)) {
        UiSelectorElementRepositoryImpl(get())
    }
}

actual fun initKoin() = startKoin {
    logger(AndroidLogger(level = Level.INFO))
    modules(
        commonModule, androidModule
    )
}
