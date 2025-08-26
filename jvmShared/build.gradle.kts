plugins {
    kotlin("jvm")
}

group = "io.appium.multiplatform"
version = "1.0"

// Shared code for submodule `:server` (Android and JVM) and `:client`

dependencies {
    // Use implementation dependencies to avoid exposing unnecessary dependencies to external modules
    implementation(project.dependencies.enforcedPlatform(projects.platform))
    implementation(kotlin("reflect"))
    implementation(libs.slf4j.simple)

    implementation(libs.kotlin.logging)
    implementation(libs.bundles.ktor.shared)
    implementation(libs.cache4k)
    implementation(libs.protobuf.kotlin)
    implementation(libs.protobuf.java.util)
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
