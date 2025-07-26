plugins {
    kotlin("jvm")
}

group = "io.appium.multiplatform"
version = "1.0"


dependencies {
    implementation(project.dependencies.enforcedPlatform(project(":platform")))
    implementation(kotlin("reflect"))
    implementation(libs.slf4j.simple)
}
