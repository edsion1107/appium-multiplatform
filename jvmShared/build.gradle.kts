plugins {
    kotlin("jvm")
}

group = "io.appium.multiplatform"
version = "1.0"

dependencies {
    implementation(project.dependencies.enforcedPlatform(projects.platform))
    implementation(kotlin("reflect"))
    implementation(libs.slf4j.simple)
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
