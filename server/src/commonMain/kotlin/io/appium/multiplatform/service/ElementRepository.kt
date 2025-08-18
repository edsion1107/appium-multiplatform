package io.appium.multiplatform.service

import com.squareup.wire.OneOf

interface ElementRepository {

    fun findElement(selector: OneOf<*, *>): OneOf<*, *>

    fun findElements(selector: OneOf<*, *>): List<OneOf<*, *>>

}

