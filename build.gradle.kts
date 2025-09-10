buildscript {
    dependencies {
        classpath(libs.zipline.gradle.plugin)
    }
}
plugins {
    alias(libs.plugins.project.report) apply true
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.ktx.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktx.atomicfu) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
//    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.buf) apply false
    alias(libs.plugins.kover)
}

tasks.register("clean", Delete::class).configure {
    group = "build"
    description = "Delete the build directory"
    delete(
        rootProject.layout.buildDirectory,
    )
}
dependencies {
    kover(projects.shared)
    kover(projects.server)
    kover(projects.client)
}