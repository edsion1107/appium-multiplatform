package io.appium.multiplatform

import io.appium.multiplatform.service.ElementHandler
import io.appium.multiplatform.service.ElementRepository
import io.appium.multiplatform.service.ElementRepositoryImpl
import io.appium.multiplatform.service.WebdriverServiceServer
import io.appium.multiplatform.service.WebdriverServiceServerImpl
import org.koin.dsl.module

val commonModule = module {
    //TODO: 添加koin-test的checkModules以进行编译时安全检查，相比 annotations + KSP 编译更快、逻辑更灵活
    single<ElementRepository> { ElementRepositoryImpl(getAll<ElementHandler<*, *>>().toSet()) }
    single<WebdriverServiceServer> {
        WebdriverServiceServerImpl(get())
    }
}