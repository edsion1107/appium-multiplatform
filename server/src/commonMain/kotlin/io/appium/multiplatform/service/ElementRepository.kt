package io.appium.multiplatform.service

interface ElementRepository<S, E> {
    fun findElement(selector: S): E
    fun findElements(selector: S): List<E>
}

enum class ElementRepositoryName {
    BY_SELECTOR, UI_SELECTOR
}
