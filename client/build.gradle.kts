import build.buf.gradle.BUF_BUILD_DIR
import build.buf.gradle.GENERATED_DIR
import build.buf.gradle.GenerateTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.buf)
    alias(libs.plugins.kotest)
    alias(libs.plugins.ksp)
}

group = "io.appium.multiplatform"
version = "unspecified"

dependencies {
    // 这是独立于 server 的端到端测试
    // 特点 1：不依赖 server 代码，必须额外启动真实的 Android 或 jvm 的 server，不能用ktor-server-test-host-jvm模拟
    // 特点 2： protobuf 不是使用的 wire 而是 buf 生成的（类名也一致不允许导入），仅限于 jvm 并且支持 validate
    implementation(project.dependencies.enforcedPlatform(projects.platform))
    implementation(projects.shared)
    implementation(projects.jvmShared)
    implementation(libs.kotest.runner.junit5)
    implementation(libs.kotest.extensions.koin.jvm)
    implementation(libs.kotest.assertions.ktor)
    implementation(libs.kotest.extensions.allure)
    implementation(libs.bundles.ktor.client)
//    implementation(libs.adblib)
    implementation(libs.protobuf.kotlin)
    implementation(libs.grpc.kotlin.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.bundles.protovalidate) {
//        exclude(group = "com.google.code.findbugs", module = "annotations")
//        because("Duplicate class javax.annotation.CheckForNull with jsr305")
    }

}

tasks.test {
    useJUnitPlatform()
    reports {
        html.required.set(true)
        junitXml.required.set(false)
    }
//    systemProperty("allure.results.directory", project.layout.buildDirectory.dir("allure-results"))
}

sourceSets {
    main {
        // https://github.com/bufbuild/buf-gradle-plugin/issues/190
        java.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/java"))
        kotlin.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/kotlin"))
    }

}
buf {
    configFileLocation = rootProject.file("${projects.shared.name}/buf.yaml")
    // 理论上是 protobuf 都是一致的，实现上的不同应该没有影响
    // wire 不支持 python、nodejs 等语言，所以 buf 生成客户端代码
    // buf 还支持 lint 和 format
    publishSchema = false
    enforceFormat = true
    build { }
    generate {
        includeImports = false
        // 注意此处`buf.gen.yaml`要与`shared/buf.gen.yaml`（cli）中插件尽量保持一致（仅inputs等配置有区别）
        templateFileLocation = layout.projectDirectory.file("buf.gen.yaml").asFile
    }

}
val generateTasks = tasks.withType<GenerateTask>()

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateTasks)
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(generateTasks)
}

