import com.google.protobuf.gradle.ExecutableLocator
import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.GenerateProtoTask.generateCmds
import com.google.protobuf.gradle.GenerateProtoTask.getCmdLengthLimit
import com.google.protobuf.gradle.ProtobufExtension
import com.google.protobuf.gradle.ToolsLocator

plugins {
    kotlin("jvm")
    alias(libs.plugins.protobuf)
}

group = "io.appium.multiplatform"
version = "1.0"


dependencies {
    implementation(project.dependencies.enforcedPlatform(project(":platform")))
    implementation(kotlin("reflect"))
    implementation(libs.slf4j.simple)
    api(libs.protobuf.java.util)
    api(libs.protobuf.kotlin)   // include protobuf-java
}

protobuf {
    protoc {
        artifact = libs.protoc.get()
            .toString() // Must not be added as an implementation dependency; this ensures the executable is correctly resolved
    }
}

tasks.withType<GenerateProtoTask>().configureEach {
    builtins {
        named("java") {
            // Do not enable lite mode — required for compatibility with JSON serialization/deserialization
//            option("lite")
        }
    }

    // Add extra code generation targets (e.g., Python) via reflection — avoids requiring additional plugins
    val extension = project.extensions.findByType<ProtobufExtension>()
    val toolsField = ProtobufExtension::class.java.getDeclaredField("tools")
    toolsField.isAccessible = true
    val tools = (toolsField.get(extension) as? ToolsLocator)

    val computeExecutablePathMethod =
        GenerateProtoTask::class.java.getDeclaredMethod("computeExecutablePath", ExecutableLocator::class.java)
    computeExecutablePathMethod.isAccessible = true

    val protocPath = computeExecutablePathMethod.invoke(this, tools?.protoc)
    logger.info("protocPath: $protocPath")

    val baseCmd = mutableListOf(protocPath.toString())
    baseCmd.addAll(includeDirs.filter { it.exists() && it.name.endsWith("proto") }.map { "-I${it.path}" })

    baseCmd.add("--kotlin_out=${outputBaseDir}/java")       // Consider also adding --python_out or --pyi_out if needed


    baseCmd.add("--python_out=${outputBaseDir}/python")
    baseCmd.add("--pyi_out=${outputBaseDir}/python")

    logger.info("baseCmd : $baseCmd, outputBaseDir: $outputBaseDir")

    val cmdResult = generateCmds(baseCmd, sourceDirs.asFileTree.files.toMutableList(), getCmdLengthLimit())
        .map {
            providers.exec {
                commandLine(it)
                isIgnoreExitValue = true // Capture stdout and stderr for manual inspection
            }
        }

    doLast {
        File(outputBaseDir, "python").mkdirs()
        cmdResult.forEach { execOutput ->
            val result = execOutput.result.get()
            execOutput.standardError.asText.get().let {
                if (it.isNotBlank()) {
                    logger.error("stderr: {}", execOutput.standardError.asText.get())
                }
            }
            execOutput.standardOutput.asText.get().let {
                if (it.isNotBlank()) {
                    logger.info("stdout: {}", execOutput.standardOutput.asText.get())
                }
            }
            // result.rethrowFailure() // Not compatible — does not throw as expected
            result.assertNormalExitValue()
        }
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