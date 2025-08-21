package io.appium.multiplatform.service

import android.annotation.SuppressLint
import android.os.Build
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.appium.multiplatform.server.BuildConfig
import com.squareup.wire.OneOf
import io.appium.multiplatform.model.*
import io.appium.multiplatform.model.StatusException.Companion.buildWebdriverException
import io.appium.multiplatform.model.error.WebDriverErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.regex.Pattern

class BySelectorHandler(
    val uiDevice: UiDevice,
    override val selectorType: OneOf.Key<BySelector>,
    override val elementType: OneOf.Key<UiObject2>,
) : ElementHandler<BySelector, UiObject2> {
    private val logger = if (BuildConfig.DEBUG) {
        KotlinLogging.logger {}
    } else {
        null
    }

    @Throws(StatusException::class)
    override fun findElement(selector: BySelector): UiObject2 {
        val by = selector.toBySelector()
        val object2 = uiDevice.findObject(by)
        if (object2 == null) {
            throw buildWebdriverException(WebDriverErrorCode.WD_NO_SUCH_ELEMENT, logger)
            {
                put("uiDevice", uiDevice)
                put("selector<${BySelector::class.qualifiedName}>", selector)
                put("by(${androidx.test.uiautomator.BySelector::class.qualifiedName})", by)
            }
        } else {
            return object2.toUiObject2()
        }
    }

    override fun findElements(selector: BySelector): List<UiObject2> {
        TODO("Not yet implemented")
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun BySelector.toBySelector(): androidx.test.uiautomator.BySelector {

        var by: androidx.test.uiautomator.BySelector? = null
        clazz?.let { by = by?.clazz(it) ?: By.clazz(it) }
        desc?.let { by = by?.desc(it) ?: By.desc(it) }
        pkg?.let { by = by?.pkg(it) ?: By.pkg(it) }
        res?.let { by = by?.res(it) ?: By.res(it) }
        text?.let { by = by?.text(it) ?: By.text(it) }
        checkable?.let { by = by?.checkable(it) ?: By.checkable(it) }
        checked?.let { by = by?.checked(it) ?: By.checked(it) }
        clickable?.let { by = by?.clickable(it) ?: By.clickable(it) }
        enabled?.let { by = by?.enabled(it) ?: By.enabled(it) }
        focusable?.let { by = by?.focusable(it) ?: By.focusable(it) }
        focused?.let { by = by?.focused(it) ?: By.focused(it) }
        long_clickable?.let { by = by?.longClickable(it) ?: By.longClickable(it) }
        scrollable?.let { by = by?.scrollable(it) ?: By.scrollable(it) }
        selected?.let { by = by?.selected(it) ?: By.selected(it) }
        depth?.let { by = by?.depth(it) ?: By.depth(it) }
        display_id?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                by = by?.displayId(it) ?: By.displayId(it)
            }
        }
        hint?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                by = by?.hint(it) ?: By.hint(it)
            }
        }

        hint_pattern?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Pattern.compile(it.text, it.flags).let { pattern ->
                    by = by?.hint(pattern) ?: By.hint(pattern)
                }
            }
        }
        clazz_pattern?.let {
            Pattern.compile(it.text, it.flags).let { pattern ->
                by = by?.clazz(pattern) ?: By.clazz(pattern)
            }
        }
        desc_pattern?.let {
            Pattern.compile(it.text, it.flags).let { pattern ->
                by = by?.desc(pattern) ?: By.desc(pattern)
            }
        }
        pkg_pattern?.let {
            Pattern.compile(it.text, it.flags).let { pattern ->
                by = by?.pkg(pattern) ?: By.pkg(pattern)
            }
        }
        res_pattern?.let {
            Pattern.compile(it.text, it.flags).let { pattern ->
                by = by?.res(pattern) ?: By.res(pattern)
            }
        }
        text_pattern?.let {
            Pattern.compile(it.text, it.flags).let { pattern ->
                by = by?.text(pattern) ?: By.text(pattern)
            }
        }
        copy?.let { by = By.copy(requireNotNull(by)) }
        has_ancestor?.let { by?.hasAncestor(it.toBySelector()) }
        has_child?.let { by = by?.hasChild(it.toBySelector()) }
        has_descendant?.let { by = by?.hasDescendant(it.toBySelector()) }
        has_parent?.let { by = by?.hasParent(it.toBySelector()) }
        return by!!
    }

    private fun android.graphics.Rect.toRect() = Rect(
        left = this.left,
        top = this.top,
        right = this.right,
        bottom = this.bottom,
    )

    private fun android.graphics.Point.toPoint() = Point(
        x = this.x,
        y = this.y
    )

    private fun androidx.test.uiautomator.UiObject2.toUiObject2() = UiObject2(
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
}