package io.appium.multiplatform.service


import io.appium.multiplatform.model.BySelector
import io.appium.multiplatform.model.UiObject2
import io.appium.multiplatform.model.toUiObject2
import io.github.oshai.kotlinlogging.KotlinLogging

class FakeBySelectorElementRepositoryImpl() : ElementRepository<BySelector, UiObject2> {
    private val logger = KotlinLogging.logger {}

    override fun findElement(selector: BySelector): UiObject2 {
        return selector.toUiObject2().also {
            logger.info { "selector: $selector, uiObject2: $it" }
        }

    }

    override fun findElements(selector: BySelector): List<UiObject2> {
        return listOf(selector.toUiObject2()).also {
            logger.info { "selector: $selector, uiObject2 list: ${it.joinToString(",")}" }
        }
    }
}