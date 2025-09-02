pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("settings-plugin")
}
rootProject.name = "appium-multiplatform"

includeBuild("build-logic")
include(":platform")
include(":shared")
include(":server")
include(":client")