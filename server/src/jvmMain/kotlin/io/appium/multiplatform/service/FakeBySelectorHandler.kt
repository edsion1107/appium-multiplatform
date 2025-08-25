package io.appium.multiplatform.service

import com.squareup.wire.OneOf
import io.appium.multiplatform.model.BySelector
import io.appium.multiplatform.model.UiObject2
import io.appium.multiplatform.model.toUiObject2
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 *
 * @see [BySelectorHandler]
 */
class FakeBySelectorHandler(
    override val selectorType: OneOf.Key<BySelector>,
    override val elementType: OneOf.Key<UiObject2>
) : ElementHandler<BySelector, UiObject2> {
    private val logger = KotlinLogging.logger {}
    override fun findElement(selector: BySelector): UiObject2 {
        logger.debug { "selector: $selector" }
        return selector.toUiObject2().also {
            logger.debug { "uiObject2: $it" }
        }
    }

    override fun findElements(selector: BySelector): List<UiObject2> {
        TODO("Not yet implemented")
    }
}