package io.appium.multiplatform.model

import com.squareup.wire.Message
import kotlin.reflect.KClass


fun isWireMessageKClass(kClass: KClass<*>): Boolean =
    kClass == Message::class || kClass.supertypes.any {
        (it.classifier as? KClass<*>)?.let(::isWireMessageKClass) == true
    }



