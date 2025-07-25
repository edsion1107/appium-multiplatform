plugins {
    alias(libs.plugins.project.report) apply true
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.kotlinx.atomicfu) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.convention.plugin) apply false
}

tasks.register("clean", Delete::class).configure {
    group = "build"
    description = "Delete the build directory"
    delete(
        rootProject.layout.buildDirectory,
    )
}