package io.appium.multiplatform.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

class KotestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply("io.kotest")
        target.plugins.apply("com.google.devtools.ksp")

        target.tasks.withType<Test>().configureEach {

            // Instead of manually supplying the '--rerun' parameter, force every test to run
            // https://github.com/kotest/kotest/issues/5076
            outputs.upToDateWhen {
                logger.lifecycle("UP-TO-DATE check for $name is disabled, forcing it to run.")
                false
            }

            useJUnitPlatform()
            reports {
                html.required.set(true)
                junitXml.required.set(false)
            }
            systemProperties(
                mapOf(
                    "kotest.framework.dump.config" to true,
                    "kotest.framework.assertion.globalassertsoftly" to true,
                    //During unit tests the init method is never called, so configure the KotlinLogging logger globally here.
                    "org.slf4j.simpleLogger.logFile" to "System.out",
                    "org.slf4j.simpleLogger.defaultLogLevel" to "INFO",
                )
            )
        }
    }
}