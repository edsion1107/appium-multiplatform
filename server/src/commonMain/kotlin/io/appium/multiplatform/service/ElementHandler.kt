package io.appium.multiplatform.service

import com.squareup.wire.OneOf

interface ElementHandler<IN : Any, OUT : Any> {
    val selectorType: OneOf.Key<IN>
    val elementType: OneOf.Key<OUT>

    fun findElement(selector: IN): OUT
    fun findElements(selector: IN): List<OUT>
}