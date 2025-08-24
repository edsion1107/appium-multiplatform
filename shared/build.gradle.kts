buildscript {
    dependencies {
        classpath(libs.wiregrpcserver.generator)
    }
}
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.wire)
    alias(libs.plugins.buf)
}

group = "io.appium.multiplatform"
version = "unspecified"

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project.dependencies.enforcedPlatform(projects.platform))
                api(kotlin("reflect"))
                api(libs.bundles.ktor.shared)
                api(libs.kotlin.logging)
                api(libs.ktx.datetime)
                api(libs.ktx.serialization.json)
            }
        }
        jvmMain {
//            kotlin.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/kotlin"))
        }
    }
}
wire {
    protoLibrary = true
    protoPath {
        srcJar {
            libs.protovalidate
        }
    }
    protoPath {
        srcJar { libs.google.common.protos }
        include("google/api/annotations.proto", "google/api/http.proto")
    }
    kotlin {
        android = false
        javaInterop = false
        buildersOnly = false
        emitDeclaredOptions = false
        boxOneOfsMinSize = 2
        rpcCallStyle = "suspending"
        rpcRole = "server"
        singleMethodServices = false
    }

}


//sourceSets {
//    named("jvmMain"){
//        // https://github.com/bufbuild/buf-gradle-plugin/issues/190
//        java.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/java"))
////        kotlin.srcDirs(layout.buildDirectory.file("$BUF_BUILD_DIR/$GENERATED_DIR/kotlin"))
//    }
//}
buf {
    // 理论上是 protobuf 都是一致的，实现上的不同应该没有影响
    // wire 不支持 python、nodejs 等语言，所以 buf 生成客户端代码
    // buf 还支持 lint 和 format
    publishSchema = false
    enforceFormat = true
    build { }
    generate {
        includeImports = false
    }
}
//val generateTasks = tasks.withType<GenerateTask>()
//
//tasks.withType<KotlinCompile>().configureEach {
//    dependsOn(generateTasks)
//}
//
//tasks.withType<JavaCompile>().configureEach {
//    dependsOn(generateTasks)
//}