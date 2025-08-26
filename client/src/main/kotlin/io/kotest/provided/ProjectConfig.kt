package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.allure.AllureTestReporter

class ProjectConfig : AbstractProjectConfig() {
    override val extensions: List<Extension> = listOf(AllureTestReporter())
}