import build.buf.gradle.BUF_BUILD_DIR
import build.buf.gradle.GENERATED_DIR
import build.buf.gradle.GenerateTask
import build.buf.gradle.ImageFormat
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.buf)
    alias(libs.plugins.ktx.serialization)
}

group = "io.appium.multiplatform"
version = "1.0"

/**
 * 在 server （Android 和 JVM 之间）和 client （JVM）之间共享的代码，主要是一些 JVM only 的工具类和方法。
 * server 和shared 模块都是 KMP 的，用来共享平台特有的、不是 JVM Only 的代码（要求 target 保持一致）。
 * 需要特别说明的是，protobuf、grpc 的官方实现都是 JVM only 的，wire、pbandk 等才有限支持 KMP。
 * 另外，这两个子模块也可以用来传递依赖，按照代码共享一样的逻辑来组织。
 */

dependencies {
    // Use implementation dependencies to avoid exposing unnecessary dependencies to external modules
    implementation(project.dependencies.enforcedPlatform(projects.platform))
    implementation(kotlin("reflect"))
    implementation(libs.slf4j.simple)

    implementation(libs.kotlin.logging)
    implementation(libs.bundles.ktor.shared)
    implementation(libs.cache4k)
    api(libs.protobuf.kotlin)
    api(libs.protobuf.java.util)
    api(libs.bundles.protovalidate){
        exclude(group="com.google.code.findbugs",module="annotations")
        because("Duplicate class javax.annotation.CheckForNull with jsr305")
    }
}

val copyCommonMainResources by tasks.registering(Copy::class) {
    val processResources by tasks.named("processResources", ProcessResources::class.java)
    val commonMainResources = project.layout.projectDirectory.files("../server/src/commonMain/resources")
    logger.info("commonMainResources: ${commonMainResources.asPath}")
    from(commonMainResources)
    into(processResources.destinationDir)
    mustRunAfter(processResources)
}

tasks.withType(Jar::class.java).configureEach {
    dependsOn(copyCommonMainResources)
}
buf {
    // 理论上是 protobuf 都是一致的，实现上的不同应该没有影响
    // wire 不支持 python、nodejs 等语言，对于 oneOf 引入了包装类型处理起来太复杂
    // buf 还支持 lint 和 format
//    configFileLocation = rootProject.file("buf.yaml")
    publishSchema = false
    enforceFormat = true
    build {
        imageFormat = ImageFormat.JSON
    }
    generate {
        includeImports = false
    }
}
sourceSets {
    main{
        // https://github.com/bufbuild/buf-gradle-plugin/issues/190
        java.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/java"))
        kotlin.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/kotlin"))
    }
}

val generateTasks = tasks.withType<GenerateTask>()

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateTasks)
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(generateTasks)
}