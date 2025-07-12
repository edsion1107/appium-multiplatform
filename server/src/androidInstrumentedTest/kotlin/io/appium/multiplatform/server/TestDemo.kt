package io.appium.multiplatform.server

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestDemo {

    val logger = KotlinLogging.logger { }

    @Before
    fun setUp() {
        logger.info { "setup" }
//        val activityClass =Class.forName("com.heytap.yoli.maintabact.MainTabActivity")
        ActivityScenario.launch<Activity>(Intent().apply {
            setClassName("com.heytap.yoli", "com.heytap.yoli.maintabact.MainTabActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    @Test()
    fun runTest() {
        logger.info { "test" }
    }

    fun tearDown() {
        logger.info { "tearDown" }
    }

}