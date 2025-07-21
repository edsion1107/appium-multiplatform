plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.tools.common)
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlinAndroid.gradlePlugin)
    compileOnly(libs.bundles.appRuntime.plugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("AndroidConventionPlugin") {
            id = "AndroidConventionPlugin"
            implementationClass = "io.appium.multiplatform.convention.AndroidConventionPlugin"
        }
        register("AppRuntimePlugin") {
            id = "AppRuntimePlugin"
            implementationClass = "io.appium.multiplatform.convention.AppRuntimePlugin"
        }
    }
}
