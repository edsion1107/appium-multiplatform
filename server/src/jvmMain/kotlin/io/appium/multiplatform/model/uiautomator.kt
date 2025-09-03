package io.appium.multiplatform.model

/**
 * for testing
 */
fun BySelector.toUiObject2(): UiObject2 {
    return uiObject2 {
        (this@toUiObject2.clazz ?: this@toUiObject2.clazzPattern?.text)?.also { className = it }
        (this@toUiObject2.desc ?: this@toUiObject2.descPattern?.text)?.also { contentDescription = it }
        (this@toUiObject2.pkg ?: this@toUiObject2.pkgPattern?.text)?.also { applicationPackage = it }
        (this@toUiObject2.res ?: this@toUiObject2.resPattern?.text)?.also { resourceName = it }
        (this@toUiObject2.text ?: this@toUiObject2.textPattern?.text)?.also { text = it }
        (this@toUiObject2.hint ?: this@toUiObject2.hintPattern?.text)?.also { hint = it }
        isCheckable = this@toUiObject2.checkable
        isChecked = this@toUiObject2.checked
        isClickable = this@toUiObject2.clickable
        isEnabled = this@toUiObject2.enabled
        isFocusable = this@toUiObject2.focusable
        isFocused = this@toUiObject2.focused
        isLongClickable = this@toUiObject2.longClickable
        isScrollable = this@toUiObject2.scrollable
        isSelected = this@toUiObject2.selected
        displayId = this@toUiObject2.displayId
    }
}