apply(from = "./gradle/settings/convention.settings.gradle.kts")
rootProject.name = "appium-multiplatform"

includeBuild("build-logic")
include(":platform")
include(":server")
