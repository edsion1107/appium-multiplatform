package io.appium.multiplatform.jvm

fun configSlf4j() {
    setSystemPropertyIfAbsent("org.slf4j.simpleLogger.logFile", "System.out")
    setSystemPropertyIfAbsent("org.slf4j.simpleLogger.defaultLogLevel", "INFO")
}