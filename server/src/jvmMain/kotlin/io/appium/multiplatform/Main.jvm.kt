package io.appium.multiplatform

import io.appium.multiplatform.jvm.setSystemPropertyIfAbsent
import io.appium.multiplatform.model.BySelector
import io.appium.multiplatform.model.UiObject2
import io.appium.multiplatform.service.ElementRepository
import io.appium.multiplatform.service.ElementRepositoryName
import io.appium.multiplatform.service.FakeBySelectorElementRepositoryImpl
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.logger.SLF4JLogger


actual val logger: KLogger = run {
    setSystemPropertyIfAbsent("org.slf4j.simpleLogger.logFile", "System.out")
    setSystemPropertyIfAbsent("org.slf4j.simpleLogger.defaultLogLevel", "INFO")
    KotlinLogging.logger {}
}


actual fun init() {
    println("init, ${logger.name}")
    logger.debug { "init" }
    Thread.currentThread().contextClassLoader.getResource("application.yaml")
        ?.let { logger.info { "Starting application at $it" } }
}

val jvmModule = module {
    single<ElementRepository<BySelector, UiObject2>>(named(ElementRepositoryName.BY_SELECTOR)) { FakeBySelectorElementRepositoryImpl() }
}

actual fun initKoin() = startKoin {
    logger(SLF4JLogger(level = Level.INFO))
    modules(
        commonModule, jvmModule
    )
}
