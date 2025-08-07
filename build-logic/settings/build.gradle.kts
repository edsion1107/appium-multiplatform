plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("settings-plugin") {
            id = "settings-plugin"
            implementationClass = "io.appium.multiplatform.convention.SettingsPlugin"
        }
    }
}