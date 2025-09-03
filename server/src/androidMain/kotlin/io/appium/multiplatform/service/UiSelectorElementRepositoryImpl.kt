package io.appium.multiplatform.service

import androidx.test.uiautomator.UiDevice
import io.appium.multiplatform.model.UiObject
import io.appium.multiplatform.model.UiSelector


class UiSelectorElementRepositoryImpl(provider: UiDeviceProvider) : ElementRepository<UiSelector, UiObject> {
    private val uiDevice: UiDevice = provider.get()
    override fun findElement(selector: UiSelector): UiObject {
        TODO("Not yet implemented")
    }

    override fun findElements(selector: UiSelector): List<UiObject> {
        TODO("Not yet implemented")
    }
}