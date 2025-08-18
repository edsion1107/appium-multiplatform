package io.appium.multiplatform.service

import androidx.test.uiautomator.UiDevice
import com.squareup.wire.OneOf
import io.appium.multiplatform.model.BySelector
import io.appium.multiplatform.model.UiObject2
import io.github.oshai.kotlinlogging.KotlinLogging
import androidx.test.uiautomator.BySelector as _BySelector
import androidx.test.uiautomator.UiObject2 as _UiObject2

class BySelectorHandler(
    val uiDevice: UiDevice,
    override val selectorType: OneOf.Key<BySelector>,
    override val elementType: OneOf.Key<UiObject2>,
) : ElementHandler<BySelector, UiObject2> {
    private val logger = KotlinLogging.logger {}
    override fun findElement(selector: BySelector): UiObject2 {
        logger.info { uiDevice }

        //TODO:找不到控件应该抛出异常
        return uiDevice.findObject(selector.toBySelector()).toUiObject2()
    }

    override fun findElements(selector: BySelector): List<UiObject2> {
        TODO("Not yet implemented")
    }

    private fun BySelector.toBySelector(): _BySelector {
        var by: _BySelector? = null
        TODO("Not yet implemented")
        return by!!
    }

    private fun _UiObject2.toUiObject2(): UiObject2 {
        TODO("Not yet implemented")
        return UiObject2(class_name = this.className)
    }
}