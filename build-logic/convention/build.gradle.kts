plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.tools.common)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlinAndroid.gradlePlugin)
    implementation(libs.kotlinx.coroutines.core.jvm)
    implementation(libs.adblib)
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
