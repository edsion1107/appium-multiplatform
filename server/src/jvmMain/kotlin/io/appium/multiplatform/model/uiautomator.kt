package io.appium.multiplatform.model

/**
 * for testing
 */
fun BySelector.toUiObject2() = UiObject2(
    class_name = clazz ?: clazz_pattern?.text,
    content_description = desc ?: desc_pattern?.text,
    application_package = pkg ?: pkg_pattern?.text,
    resource_name = res ?: res_pattern?.text,
    text = text ?: text_pattern?.text,
    is_checkable = checkable,
    is_checked = checked,
    is_clickable = clickable,
    is_enabled = enabled,
    is_focusable = focusable,
    is_focused = focused,
    is_long_clickable = long_clickable,
    is_scrollable = scrollable,
    is_selected = selected,
    display_id = display_id,
    hint = hint ?: hint_pattern?.text,
    visible_bounds = null,
    visible_center = null,
    drawing_order = null,
    child_count = null
)