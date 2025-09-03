package io.appium.multiplatform.service

import androidx.test.uiautomator.UiDevice
import com.appium.multiplatform.server.BuildConfig
import io.appium.multiplatform.jvm.StatusException.Companion.buildWebdriverException
import io.appium.multiplatform.model.BySelector
import io.appium.multiplatform.model.UiObject2
import io.appium.multiplatform.model.error.WebDriverErrorCode
import io.appium.multiplatform.model.toBySelector
import io.appium.multiplatform.model.toProto
import io.github.oshai.kotlinlogging.KotlinLogging


class BySelectorElementRepositoryImpl(provider: UiDeviceProvider) : ElementRepository<BySelector, UiObject2> {
    private val uiDevice: UiDevice = provider.get()
    private val logger = if (BuildConfig.DEBUG) {
        KotlinLogging.logger {}
    } else {
        null
    }

    override fun findElement(selector: BySelector): UiObject2 {
        val by = selector.toBySelector()
        val uiObject2 = uiDevice.findObject(by)
        if (uiObject2 == null) {
            throw buildWebdriverException(WebDriverErrorCode.WD_NO_SUCH_ELEMENT, logger) {
                put("uiDevice", uiDevice)
                put("selector<${BySelector::class.qualifiedName}>", selector)
                put("by(${androidx.test.uiautomator.BySelector::class.qualifiedName})", by)
            }
        } else {
            return uiObject2.toProto()
        }
    }

    override fun findElements(selector: BySelector): List<UiObject2> {
        val by = selector.toBySelector()
        val uiObject2List = uiDevice.findObjects(by)
        if (uiObject2List.isEmpty()) {
            throw buildWebdriverException(WebDriverErrorCode.WD_NO_SUCH_ELEMENT, logger) {
                put("uiDevice", uiDevice)
                put("selector<${BySelector::class.qualifiedName}>", selector)
                put("by(${androidx.test.uiautomator.BySelector::class.qualifiedName})", by)
            }
        } else {
            return uiObject2List.map { it.toProto() }
        }
    }
}