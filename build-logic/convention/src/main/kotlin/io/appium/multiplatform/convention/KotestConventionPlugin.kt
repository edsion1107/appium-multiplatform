package io.appium.multiplatform.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import kotlin.text.set

class KotestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply("io.kotest")
        target.plugins.apply("com.google.devtools.ksp")

        target.tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            reports {
                html.required.set(true)
                junitXml.required.set(false)
            }
            systemProperties(
                mapOf(
                    "kotest.framework.dump.config" to true,
                    "kotest.framework.config.fqn" to "io.appium.multiplatform.ProjectConfig",
                )
            )
        }
    }
}