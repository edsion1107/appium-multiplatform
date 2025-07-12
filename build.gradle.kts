plugins {
    alias(libs.plugins.project.report) apply true
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.android.convention.plugin) apply false
}

tasks.register("clean", Delete::class).configure {
    group = "build"
    description = "Delete the build directory"
    delete(
        rootProject.layout.buildDirectory,
    )
}