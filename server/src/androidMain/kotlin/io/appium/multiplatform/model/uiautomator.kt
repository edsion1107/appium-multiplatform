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

    // --- 1. Build base selectors ---
    // `this` refers to the custom BySelector instance, from which we get property values.
    // Using Lambda to resolve overloading issues.
    uiSelector = uiSelector.chain(this.clazz, { clazz(it) }, { By.clazz(it) })
    uiSelector = uiSelector.chain(this.desc, { desc(it) }, { By.desc(it) })
    uiSelector = uiSelector.chain(this.pkg, { pkg(it) }, { By.pkg(it) })
    uiSelector = uiSelector.chain(this.res, { res(it) }, { By.res(it) })
    uiSelector = uiSelector.chain(this.text, { text(it) }, { By.text(it) })

    // For methods without overloads, method references are more concise.
    uiSelector =
        uiSelector.chain(this.checkable, androidx.test.uiautomator.BySelector::checkable, By::checkable)
    uiSelector = uiSelector.chain(this.checked, androidx.test.uiautomator.BySelector::checked, By::checked)
    uiSelector =
        uiSelector.chain(this.clickable, androidx.test.uiautomator.BySelector::clickable, By::clickable)
    uiSelector = uiSelector.chain(this.enabled, androidx.test.uiautomator.BySelector::enabled, By::enabled)
    uiSelector =
        uiSelector.chain(this.focusable, androidx.test.uiautomator.BySelector::focusable, By::focusable)
    uiSelector = uiSelector.chain(this.focused, androidx.test.uiautomator.BySelector::focused, By::focused)
    uiSelector = uiSelector.chain(
        this.long_clickable,
        androidx.test.uiautomator.BySelector::longClickable,
        By::longClickable
    )
    uiSelector =
        uiSelector.chain(this.scrollable, androidx.test.uiautomator.BySelector::scrollable, By::scrollable)
    uiSelector = uiSelector.chain(this.selected, androidx.test.uiautomator.BySelector::selected, By::selected)
    uiSelector = uiSelector.chain(this.depth, androidx.test.uiautomator.BySelector::depth, By::depth)

    // Properties with API version checks.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        uiSelector =
            uiSelector.chain(this.display_id, androidx.test.uiautomator.BySelector::displayId, By::displayId)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        uiSelector = uiSelector.chain(this.hint, { hint(it) }, { By.hint(it) })
        this.hint_pattern?.let {
            val pattern = Pattern.compile(it.text, it.flags)
            uiSelector = uiSelector.chain(pattern, { hint(it) }, { By.hint(it) })
        }
    }

    // Pattern properties.
    this.clazz_pattern?.let {
        uiSelector = uiSelector.chain(Pattern.compile(it.text, it.flags), { clazz(it) }, { By.clazz(it) })
    }
    this.desc_pattern?.let {
        uiSelector = uiSelector.chain(Pattern.compile(it.text, it.flags), { desc(it) }, { By.desc(it) })
    }
    this.pkg_pattern?.let {
        uiSelector = uiSelector.chain(Pattern.compile(it.text, it.flags), { pkg(it) }, { By.pkg(it) })
    }
    this.res_pattern?.let {
        uiSelector = uiSelector.chain(Pattern.compile(it.text, it.flags), { res(it) }, { By.res(it) })
    }
    this.text_pattern?.let {
        uiSelector = uiSelector.chain(Pattern.compile(it.text, it.flags), { text(it) }, { By.text(it) })
    }

    // --- 2. Ensure selector is created, otherwise conversion is meaningless ---
    var finalSelector = requireNotNull(uiSelector) {
        "Custom BySelector must contain at least one condition to be converted."
    }

    // --- 3. Apply copy and hierarchy ---
    this.copy?.let { finalSelector = By.copy(finalSelector) }

    // Fix bug: Ensure calls to hierarchy selectors are reassigned.
    // `it.toBySelector()` is the correct recursive call.
    this.has_ancestor?.let { finalSelector = finalSelector.hasAncestor(it.toBySelector()) }
    this.has_child?.let { finalSelector = finalSelector.hasChild(it.toBySelector()) }
    this.has_descendant?.let { finalSelector = finalSelector.hasDescendant(it.toBySelector()) }
    this.has_parent?.let { finalSelector = finalSelector.hasParent(it.toBySelector()) }

    // --- 4. Return the final result ---
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
fun androidx.test.uiautomator.UiObject2.toUiObject2() = UiObject2(
    class_name = this.className,
    content_description = this.contentDescription,
    application_package = this.applicationPackage,
    resource_name = this.resourceName,
    text = this.text,
    is_checkable = this.isCheckable,
    is_checked = this.isChecked,
    is_clickable = this.isClickable,
    is_enabled = this.isEnabled,
    is_focusable = this.isFocusable,
    is_focused = this.isFocused,
    is_long_clickable = this.isLongClickable,
    is_scrollable = this.isScrollable,
    is_selected = this.isSelected,
    display_id = this.displayId,
    hint = this.hint,
    visible_bounds = this.visibleBounds.toRect(),
    visible_center = this.visibleCenter.toPoint(),
    drawing_order = this.drawingOrder,
//                child_count = this.childCount,
)

/**
 * Converts an Android graphics Rect to a custom Rect data class.
 *
 * @param this The `android.graphics.Rect` instance to convert.
 * @return A custom `Rect` data class instance.
 */
fun android.graphics.Rect.toRect() = Rect(
    left = this.left,
    top = this.top,
    right = this.right,
    bottom = this.bottom,
)

/**
 * Converts an Android graphics Point to a custom Point data class.
 *
 * @param this The `android.graphics.Point` instance to convert.
 * @return A custom `Point` data class instance.
 */
fun android.graphics.Point.toPoint() = Point(
    x = this.x,
    y = this.y
)
