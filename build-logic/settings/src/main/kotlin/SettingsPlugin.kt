@file:Suppress("UnstableApiUsage")
package io.appium.multiplatform.convention

import org.gradle.api.Plugin
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.RepositoriesMode
import java.net.URI

class SettingsPlugin : Plugin<Settings> {
    override fun apply(target: Settings) = with(target) {
        enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

        pluginManagement {
            repositories {
                gradlePluginPortal()
                mavenCentral()
                googleWithContentFilter()
                kotlinxRpcMaven()
            }
        }

        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                mavenCentral()
                googleWithContentFilter()
                kotlinxRpcMaven()
            }
        }
    }

    private fun RepositoryHandler.googleWithContentFilter() {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
    }

    private fun RepositoryHandler.kotlinxRpcMaven() {
        maven {
            url = URI("https://maven.pkg.jetbrains.space/public/p/krpc/grpc")
            content {
                includeModuleByRegex("org.jetbrains.kotlinx","kotlinx-rpc-.*")
                includeGroup("org.jetbrains.kotlinx.rpc.plugin")
            }
        }
    }
}