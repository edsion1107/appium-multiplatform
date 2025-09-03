package io.appium.multiplatform.model

import android.annotation.SuppressLint
import android.os.Build
import androidx.test.uiautomator.By
import java.util.regex.Pattern

/**
 * Converts a protobuf-based `BySelector` to an Android UiAutomator `BySelector`.
 *
 * This function takes a `BySelector` object, which is defined as a protobuf message and contains
 * various criteria for locating UI elements, and translates it into the native
 * `androidx.test.uiautomator.BySelector` format. It handles chaining multiple selection criteria
 * and accounts for API level differences.
 *
 * @return An `androidx.test.uiautomator.BySelector` instance representing the protobuf-based criteria.
 */
@SuppressLint("ObsoleteSdkInt")
fun BySelector.toBySelector(): androidx.test.uiautomator.BySelector {


    var uiSelector: androidx.test.uiautomator.BySelector? = null

    /**
     * Helper function for chaining selector conditions.
     *
     * This function allows for a fluent way to build UiAutomator selectors by applying conditions
     * to an existing `uiSelector` or creating a new one if `uiSelector` is null.
     *
     * @param T The type of the value to be applied.
     * @param value The value to apply to the selector condition.
     * @param func A lambda that applies the condition to the `uiSelector` if it's not null.
     * @param initial A lambda that creates a new selector if `uiSelector` is null.
     * @return The updated `uiSelector` or null if the value is null and `uiSelector` was null.
     */
    fun <T> androidx.test.uiautomator.BySelector?.chain(
        value: T?,
        // func: How to add a condition to uiSelector if it's not null
        func: androidx.test.uiautomator.BySelector.(T) -> androidx.test.uiautomator.BySelector,
        // initial: How to create a new selector if uiSelector is null
        initial: (T) -> androidx.test.uiautomator.BySelector
    ): androidx.test.uiautomator.BySelector? {
        return value?.let {
            this?.func(it) ?: initial(it)
        } ?: this // If value is null, keep uiSelector unchanged
    }

    fun <T> androidx.test.uiautomator.BySelector?.chain(
        value: T,
        // func: How to add a condition to uiSelector if it's not null
        func: androidx.test.uiautomator.BySelector.(T) -> androidx.test.uiautomator.BySelector,
        // initial: How to create a new selector if uiSelector is null
        initial: (T) -> androidx.test.uiautomator.BySelector
    ): androidx.test.uiautomator.BySelector {
        return this?.func(value) ?: initial(value)
    }
    // --- 1. Build base selectors ---
    // `this` refers to the custom BySelector instance, from which we get property values.
    // Using Lambda to resolve overloading issues.
    if (hasClazz()) {
        uiSelector = uiSelector.chain(clazz, androidx.test.uiautomator.BySelector::clazz, By::clazz)
    } else if (hasClazzPattern()) {
        uiSelector =
            uiSelector.chain(
                Pattern.compile(clazzPattern.text, clazzPattern.flags),
                androidx.test.uiautomator.BySelector::clazz,
                By::clazz
            )
    }
    if (hasDesc()) {
        uiSelector = uiSelector.chain(desc, androidx.test.uiautomator.BySelector::desc, By::desc)
    } else if (hasDescPattern()) {
        uiSelector = uiSelector.chain(
            Pattern.compile(descPattern.text, descPattern.flags),
            androidx.test.uiautomator.BySelector::desc,
            By::desc
        )
    }
    if (hasPkg()) {
        uiSelector = uiSelector.chain(pkg, androidx.test.uiautomator.BySelector::pkg, By::pkg)
    } else if (hasPkgPattern()) {
        uiSelector = uiSelector.chain(
            Pattern.compile(pkgPattern.text, pkgPattern.flags),
            androidx.test.uiautomator.BySelector::pkg,
            By::pkg
        )
    }
    if (hasRes()) {
        uiSelector = uiSelector.chain(res, androidx.test.uiautomator.BySelector::res, By::res)
    } else if (hasResPattern()) {
        uiSelector = uiSelector.chain(
            Pattern.compile(resPattern.text, resPattern.flags),
            androidx.test.uiautomator.BySelector::res,
            By::res
        )
    }
    if (hasText()) {
        uiSelector = uiSelector.chain(text, androidx.test.uiautomator.BySelector::text, By::text)
    } else if (hasTextPattern()) {
        uiSelector = uiSelector.chain(
            Pattern.compile(textPattern.text, textPattern.flags),
            androidx.test.uiautomator.BySelector::text,
            By::text
        )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (hasHint()) {
            uiSelector = uiSelector.chain(hint, androidx.test.uiautomator.BySelector::hint, By::hint)
        } else if (hasHintPattern()) {
            uiSelector = uiSelector.chain(
                Pattern.compile(hintPattern.text, hintPattern.flags),
                androidx.test.uiautomator.BySelector::hint,
                By::hint
            )
        }
    }
    if (hasCheckable()) {
        uiSelector = uiSelector.chain(checkable, androidx.test.uiautomator.BySelector::checkable, By::checkable)
    }
    if (hasChecked()) {
        uiSelector = uiSelector.chain(checked, androidx.test.uiautomator.BySelector::checked, By::checked)
    }
    if (hasClickable()) {
        uiSelector = uiSelector.chain(clickable, androidx.test.uiautomator.BySelector::clickable, By::clickable)
    }
    if (hasEnabled()) {
        uiSelector = uiSelector.chain(enabled, androidx.test.uiautomator.BySelector::enabled, By::enabled)
    }
    if (hasFocusable()) {
        uiSelector = uiSelector.chain(focusable, androidx.test.uiautomator.BySelector::focusable, By::focusable)
    }
    if (hasFocused()) {
        uiSelector = uiSelector.chain(focused, androidx.test.uiautomator.BySelector::focused, By::focused)
    }
    if (hasLongClickable()) {
        uiSelector =
            uiSelector.chain(longClickable, androidx.test.uiautomator.BySelector::longClickable, By::longClickable)
    }
    if (hasScrollable()) {
        uiSelector = uiSelector.chain(scrollable, androidx.test.uiautomator.BySelector::scrollable, By::scrollable)
    }
    if (hasSelected()) {
        uiSelector = uiSelector.chain(selected, androidx.test.uiautomator.BySelector::selected, By::selected)
    }
    if (hasDepth()) {
        uiSelector = uiSelector.chain(depth, androidx.test.uiautomator.BySelector::depth, By::depth)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (hasDisplayId()) {
            uiSelector = uiSelector.chain(displayId, androidx.test.uiautomator.BySelector::displayId, By::displayId)
        }
    }

    // --- 2. Ensure selector is created, otherwise conversion is meaningless ---
    var finalSelector = requireNotNull(uiSelector) {
        "Custom BySelector must contain at least one condition to be converted."
    }
    if (hasAncestor()) {
        finalSelector = finalSelector.chain(
            ancestor.toBySelector(),
            androidx.test.uiautomator.BySelector::hasAncestor,
            By::hasAncestor
        )
    }
    if (hasChild()) {
        finalSelector =
            finalSelector.chain(child.toBySelector(), androidx.test.uiautomator.BySelector::hasChild, By::hasChild)
    }
    if (hasDescendant()) {
        finalSelector = finalSelector.chain(
            descendant.toBySelector(),
            androidx.test.uiautomator.BySelector::hasDescendant,
            By::hasDescendant
        )
    }
    if (hasParent()) {
        finalSelector =
            finalSelector.chain(parent.toBySelector(), androidx.test.uiautomator.BySelector::hasParent, By::hasParent)
    }

    return finalSelector
}

/**
 * Converts an Android UiObject2 object to a custom UiObject2 data class.
 *
 * This function maps properties from the native UiObject2 to a more convenient data structure.
 *
 * @param this The `androidx.test.uiautomator.UiObject2` instance to convert.
 * @return A custom `UiObject2` data class instance.
 */
fun androidx.test.uiautomator.UiObject2.toProto() = uiObject2 {
    this@toProto.className?.also { className = it }
    this@toProto.contentDescription?.also { contentDescription = it }
    this@toProto.applicationPackage?.also { applicationPackage = it }
    this@toProto.resourceName?.also { resourceName = it }
    this@toProto.text?.also { text = it }
    this@toProto.hint?.also { hint = it }
    isCheckable = this@toProto.isCheckable
    isChecked = this@toProto.isChecked
    isClickable = this@toProto.isClickable
    isEnabled = this@toProto.isEnabled
    isFocusable = this@toProto.isFocusable
    isFocused = this@toProto.isFocused
    isLongClickable = this@toProto.isLongClickable
    isScrollable = this@toProto.isScrollable
    isSelected = this@toProto.isSelected
    displayId = this@toProto.displayId
    visibleBounds = this@toProto.visibleBounds.toProto()
    visibleCenter = this@toProto.visibleCenter.toProto()
    drawingOrder = this@toProto.drawingOrder
}


/**
 * Converts an Android graphics Rect to a custom Rect data class.
 *
 * @param this The `android.graphics.Rect` instance to convert.
 * @return A custom `Rect` data class instance.
 */
fun android.graphics.Rect.toProto() = rect {
    left = this@toProto.left
    top = this@toProto.top
    right = this@toProto.right
    bottom = this@toProto.bottom
}

/**
 * Converts an Android graphics Point to a custom Point data class.
 *
 * @param this The `android.graphics.Point` instance to convert.
 * @return A custom `Point` data class instance.
 */
fun android.graphics.Point.toProto() = point {
    x = this@toProto.x
    y = this@toProto.y
}
