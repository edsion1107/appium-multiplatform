package io.appium.multiplatform

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.test.AssertionMode

class ProjectConfig : AbstractProjectConfig() {
    override val failOnEmptyTestSuite: Boolean = true
    override val globalAssertSoftly: Boolean = true
    override val assertionMode = AssertionMode.Error
}