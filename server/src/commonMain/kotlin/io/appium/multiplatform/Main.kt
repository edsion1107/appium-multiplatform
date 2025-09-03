package io.appium.multiplatform

import io.github.oshai.kotlinlogging.KLogger
import org.koin.core.KoinApplication
import org.koin.dsl.module


/**
 * Performs platform-specific initialization and pre-checks.
 *
 * ⚠️ Execution order is not guaranteed, and code that takes too long should not be executed.
 *
 */
expect fun init()

expect val logger: KLogger

expect fun initKoin(): KoinApplication

val commonModule = module {
    //TODO: 添加koin-test的checkModules以进行编译时安全检查，相比 annotations + KSP 编译更快、逻辑更灵活
//    single<ElementRepository> { ElementRepositoryImpl(getAll<ElementHandler<*, *>>().toSet()) }
//    single<WebdriverServiceServer> {
//        WebdriverServiceServerImpl(get())
//    }

}