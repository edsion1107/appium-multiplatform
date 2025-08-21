package io.appium.multiplatform.service


import androidx.test.uiautomator.UiDevice
import com.appium.multiplatform.server.BuildConfig
import com.squareup.wire.OneOf
import io.appium.multiplatform.model.*
import io.appium.multiplatform.model.StatusException.Companion.buildWebdriverException
import io.appium.multiplatform.model.error.WebDriverErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging

class BySelectorHandler(
    val uiDevice: UiDevice,
    override val selectorType: OneOf.Key<BySelector>,
    override val elementType: OneOf.Key<UiObject2>,
) : ElementHandler<BySelector, UiObject2> {
    private val logger = if (BuildConfig.DEBUG) {
        KotlinLogging.logger {}
    } else {
        null
    }

    @Throws(StatusException::class)
    override fun findElement(selector: BySelector): UiObject2 {
        val by = selector.toBySelector()
        val object2 = uiDevice.findObject(by)
        if (object2 == null) {
            throw buildWebdriverException(WebDriverErrorCode.WD_NO_SUCH_ELEMENT, logger)
            {
                put("uiDevice", uiDevice)
                put("selector<${BySelector::class.qualifiedName}>", selector)
                put("by(${androidx.test.uiautomator.BySelector::class.qualifiedName})", by)
            }
        } else {
            return object2.toUiObject2()
        }
    }

    override fun findElements(selector: BySelector): List<UiObject2> {
        TODO("Not yet implemented")
    }
}