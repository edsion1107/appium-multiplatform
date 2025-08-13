import build.buf.gradle.GENERATED_DIR
import build.buf.gradle.BUF_BUILD_DIR
import build.buf.gradle.GenerateTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.buf)
}

group = "io.appium.multiplatform"
version = "1.0"


sourceSets {
    main {
        // https://github.com/bufbuild/buf-gradle-plugin/issues/190
        java.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/java"))
        kotlin.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/kotlin"))
    }
}

dependencies {
    implementation(project.dependencies.enforcedPlatform(projects.platform))
    implementation(kotlin("reflect"))
    implementation(libs.slf4j.simple)
    api(libs.bundles.protovalidate){
        exclude(group="com.google.code.findbugs",module="annotations")
        because("Duplicate class javax.annotation.CheckForNull with jsr305")
    }
//    api("io.envoyproxy.protoc-gen-validate:pgv-java-stub:0.6.13")
//    api("com.google.api.grpc:proto-google-common-protos:2.60.0")
    implementation(libs.grpc.kotlin.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)
    api(libs.grpc.core)
//    api("io.grpc:grpc-netty-shaded")
    api(libs.grpc.okhttp)
    api(libs.protobuf.java.util)
    api(libs.protobuf.kotlin)   // include protobuf-java
}

buf {
    publishSchema = false
    enforceFormat = true
    build { }
    generate{
        includeImports = false
    }
}

val generateTasks = tasks.withType<GenerateTask>()

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateTasks)
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(generateTasks)
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
