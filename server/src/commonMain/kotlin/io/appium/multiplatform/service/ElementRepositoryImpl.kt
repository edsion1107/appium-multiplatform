package io.appium.multiplatform.service

import com.squareup.wire.OneOf


class ElementRepositoryImpl(handlers: Set<ElementHandler<*, *>>) : ElementRepository {
    private val handlerMap = handlers.associateBy { it.selectorType }
    override fun findElement(selector: OneOf<*, *>): OneOf<*, *> {
        val handler = handlerMap[selector.key]
            ?: throw UnsupportedOperationException("No handler registered for selector key ${selector.key.declaredName}")
        return executeAndWrap(handler, requireNotNull(selector.value))
    }

    override fun findElements(selector: OneOf<*, *>): List<OneOf<*, *>> {
        TODO("Not yet implemented")
    }

    private fun <IN : Any, OUT : Any> executeAndWrap(
        handler: ElementHandler<IN, OUT>,
        value: Any
    ): OneOf<*, *> {
        @Suppress("UNCHECKED_CAST")
        val result: OUT = handler.findElement(value as IN)
        return OneOf(handler.elementType, result)
    }
}