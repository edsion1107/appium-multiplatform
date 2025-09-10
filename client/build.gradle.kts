import build.buf.gradle.BUF_BUILD_DIR
import build.buf.gradle.GENERATED_DIR

plugins {
    kotlin("jvm")
    alias(libs.plugins.project.report)
    alias(libs.plugins.convention.kotest)
    alias(libs.plugins.kover)
}

group = "io.appium.multiplatform"
version = "unspecified"
kotlin {
    compilerOptions {
        extraWarnings.set(true)   // TODO: wire plugin not support, waiting for a fix
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
dependencies {
    // 这是独立于 server 的端到端测试
    // 不依赖 server 代码，必须额外启动真实的 Android 或 jvm 的 server，不能用ktor-server-test-host-jvm模拟
    implementation(project.dependencies.enforcedPlatform(projects.platform))
    implementation(projects.shared)
    implementation(libs.kotest.runner.junit5)
    implementation(libs.kotest.extensions.koin.jvm)
    implementation(libs.kotest.assertions.ktor)
    implementation(libs.kotest.extensions.allure)
    implementation(libs.bundles.ktor.client)
}

//tasks.test {
//    useJUnitPlatform()
//    reports {
//        html.required.set(true)
//        junitXml.required.set(false)
//    }
////    systemProperty("allure.results.directory", project.layout.buildDirectory.dir("allure-results"))
//}

sourceSets {
    main {
        java.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/java"))
        kotlin.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/kotlin"))
    }

}


