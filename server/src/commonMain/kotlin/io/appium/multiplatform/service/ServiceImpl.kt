package io.appium.multiplatform.service

import com.squareup.wire.OneOf
import io.appium.multiplatform.service.FindElementResponse.Element


class WebdriverServiceServerImpl(private val repository: ElementRepository) : WebdriverServiceServer {


    override suspend fun FindElement(request: FindElementRequest): FindElementResponse {
        @Suppress("UNCHECKED_CAST")
        val res = repository.findElement(requireNotNull(request.selector)) as OneOf<Element<*>, *>
        return FindElementResponse(element = res)
    }

    override suspend fun FindElements(request: FindElementRequest): FindElementResponses {
        repository.findElements(requireNotNull(request.selector))
        return FindElementResponses()
    }

}