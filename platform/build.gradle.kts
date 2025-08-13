plugins {
    `java-platform`
}

group = "io.appium.multiplatform"
version = "1.0.0"

javaPlatform {
    allowDependencies()
}
dependencies {
    api(platform(libs.kotlin.bom))
    api(platform(libs.ktor.bom))
    api(platform(libs.koin.bom))
    api(platform(libs.kotest.bom))
    api(platform(libs.micrometer.bom))
    api(platform(libs.grpc.bom))
    constraints {
        //TODO: 如果有不是bom的约束可以在这里尝试添加
    }
}
