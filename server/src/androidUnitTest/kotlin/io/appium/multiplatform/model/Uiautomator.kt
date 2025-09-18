package io.appium.multiplatform.model


import android.os.Build
import android.view.Display.INVALID_DISPLAY
import androidx.test.uiautomator.UiObject2
import io.appium.multiplatform.jvm.ReflectiveAccess.Companion.reflectField
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.engine.names.WithDataTestName
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeZero
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.util.regex.Pattern
import kotlin.uuid.Uuid

@OptIn(ExperimentalKotest::class)
class Uiautomator : FunSpec() {
    val logger = KotlinLogging.logger {}
    val uiObject2 = mockk<UiObject2>(relaxed = true)

    init {
        context("BySelector") {

            context("properties") {
                withData(
                    listOf(
                        Property("clazz", Uuid.random().toString(), flags = null),
                        Property("clazz", Uuid.random().toString(), flags = 0),
                        Property("clazz", Uuid.random().toString(), flags = 0x32)
                    )
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mClazz")
                    (reflectField(by::class.java, "mClazz").get(by) as Pattern)
                        .pattern().shouldContain(input.value).shouldNotBeBlank()
                }
                withData(
                    listOf(
                        Property("desc", Uuid.random().toString(), flags = null),
                        Property("desc", Uuid.random().toString(), flags = 0),
                        Property("desc", Uuid.random().toString(), flags = 0x32)
                    )
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mDesc")
                    (reflectField(by::class.java, "mDesc").get(by) as Pattern)
                        .pattern().shouldContain(input.value).shouldNotBeBlank()
                }
                withData(
                    listOf(
                        Property("pkg", Uuid.random().toString(), flags = null),
                        Property("pkg", Uuid.random().toString(), flags = 0),
                        Property("pkg", Uuid.random().toString(), flags = 0x32)
                    )
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mPkg")
                    (reflectField(by::class.java, "mPkg").get(by) as Pattern)
                        .pattern().shouldContain(input.value).shouldNotBeBlank()
                }
                withData(
                    listOf(
                        Property("res", Uuid.random().toString(), flags = null),
                        Property("res", Uuid.random().toString(), flags = 0),
                        Property("res", Uuid.random().toString(), flags = 0x32)
                    )
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mRes")
                    (reflectField(by::class.java, "mRes").get(by) as Pattern)
                        .pattern().shouldContain(input.value).shouldNotBeBlank()
                }
                withData(
                    listOf(
                        Property("text", Uuid.random().toString(), flags = null),
                        Property("text", Uuid.random().toString(), flags = 0),
                        Property("text", Uuid.random().toString(), flags = 0x32)
                    )
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mText")
                    (reflectField(by::class.java, "mText").get(by) as Pattern)
                        .pattern().shouldContain(input.value).shouldNotBeBlank()
                }
                withData(
                    listOf(
                        Property("hint", Uuid.random().toString(), flags = null),
                        Property("hint", Uuid.random().toString(), flags = 0),
                        Property("hint", Uuid.random().toString(), flags = 0x32)
                    )
                ) { input ->
                    mockkStatic("io.appium.multiplatform.model.UiautomatorKt")
                    every { getBuildVersion() } returns Build.VERSION_CODES.O
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mHint")
                    (reflectField(by::class.java, "mHint").get(by) as Pattern)
                        .pattern().shouldContain(input.value).shouldNotBeBlank()
                }
                withData(
                    Property("checkable", true),
                    Property("checkable", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mCheckable")
                    (reflectField(by::class.java, "mCheckable").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    Property("checked", true),
                    Property("checked", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mChecked")
                    (reflectField(by::class.java, "mChecked").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    Property("clickable", true),
                    Property("clickable", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mClickable")
                    (reflectField(by::class.java, "mClickable").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    Property("enabled", true),
                    Property("enabled", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mEnabled")
                    (reflectField(by::class.java, "mEnabled").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    Property("focusable", true),
                    Property("focusable", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mFocusable")
                    (reflectField(by::class.java, "mFocusable").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    Property("focused", true),
                    Property("focused", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mFocused")
                    (reflectField(by::class.java, "mFocused").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    Property("long_clickable", true),
                    Property("long_clickable", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mLongClickable")
                    (reflectField(by::class.java, "mLongClickable").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    Property("scrollable", true),
                    Property("scrollable", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mScrollable")
                    (reflectField(by::class.java, "mScrollable").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    Property("selected", true),
                    Property("selected", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mSelected")
                    (reflectField(by::class.java, "mSelected").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    Property("depth", 0),
                    Property("depth", 1),
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mMinDepth")
                    by::class.shouldHaveMemberProperty("mMaxDepth")
                    (reflectField(by::class.java, "mMinDepth").get(by) as Int)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                    (reflectField(by::class.java, "mMaxDepth").get(by) as Int)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    Property("display_id", 0),
                    Property("display_id", 1),
                ) { input ->
                    mockkStatic("io.appium.multiplatform.model.UiautomatorKt")
                    every { getBuildVersion() } returns Build.VERSION_CODES.R
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mDisplayId")
                    (reflectField(by::class.java, "mDisplayId").get(by) as Int)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                context("Negative Scenarios") {
                    withData(
                        listOf(
                            Property("hint", Uuid.random().toString(), flags = null),
                            Property("hint", Uuid.random().toString(), flags = 0),
                            Property("hint", Uuid.random().toString(), flags = 0x32)
                        )
                    ) { input ->
                        mockkStatic("io.appium.multiplatform.model.UiautomatorKt")
                        every { getBuildVersion() } returns Build.VERSION_CODES.N
                        val proto = input.toProto()
                        shouldThrow<IllegalArgumentException> {
                            proto.asUiAutomator()
                        }
                    }
                    withData(
                        Property("display_id", 0),
                        Property("display_id", 1),
                    ) { input ->
                        mockkStatic("io.appium.multiplatform.model.UiautomatorKt")
                        every { getBuildVersion() } returns Build.VERSION_CODES.Q
                        val proto = input.toProto()
                        shouldThrow<IllegalArgumentException> {
                            proto.asUiAutomator()
                        }
                    }
                }
            }
        }
        context("UiObject2") {
            beforeEach {
                clearMocks(uiObject2)
            }
            test("properties") {
                val proto = uiObject2.toProto()
                //String
                proto.className.shouldBeEmpty()
                proto.contentDescription.shouldBeEmpty()
                proto.applicationPackage.shouldBeEmpty()
                proto.resourceName.shouldBeEmpty()
                proto.text.shouldBeEmpty()
                proto.hint.shouldBeEmpty()

                //bool
                proto.isCheckable.shouldBeFalse()
                proto.isChecked.shouldBeFalse()
                proto.isClickable.shouldBeFalse()
                proto.isEnabled.shouldBeFalse()
                proto.isFocusable.shouldBeFalse()
                proto.isFocused.shouldBeFalse()
                proto.isLongClickable.shouldBeFalse()
                proto.isScrollable.shouldBeFalse()
                proto.isSelected.shouldBeFalse()

                // int
                proto.displayId.shouldBeZero()
                proto.drawingOrder.shouldBeZero()
                proto.childCount.shouldBeZero()

                proto.hasVisibleBounds().shouldBeTrue()
                proto.visibleBounds.left.shouldBeZero()
                proto.visibleBounds.right.shouldBeZero()
                proto.visibleBounds.top.shouldBeZero()
                proto.visibleBounds.bottom.shouldBeZero()

                proto.hasVisibleCenter().shouldBeTrue()
                proto.visibleCenter.x.shouldBeZero()
                proto.visibleCenter.y.shouldBeZero()
            }
            test("nullable properties") {
                every { uiObject2.className } returns null
                every { uiObject2.contentDescription } returns null
                every { uiObject2.applicationPackage } returns null
                every { uiObject2.resourceName } returns null
                every { uiObject2.text } returns null
                every { uiObject2.hint } returns null
                every { uiObject2.displayId } returns INVALID_DISPLAY
                val proto = uiObject2.toProto()
                proto.hasClassName().shouldBeFalse()
                proto.hasContentDescription().shouldBeFalse()
                proto.hasApplicationPackage().shouldBeFalse()
                proto.hasResourceName().shouldBeFalse()
                proto.hasText().shouldBeFalse()
                proto.hasHint().shouldBeFalse()
                proto.hasDisplayId().shouldBeTrue()
                proto.displayId.shouldBe(-1)
            }
        }
    }


    data class Property<T>(val name: String, val value: T, val flags: Int? = null) : WithDataTestName {
        override fun dataTestName(): String = this.toString()
        fun toProto(): BySelector = when (value) {
            is Int -> toProtoInt(value)
            is String -> toProtoString(value)
            is Boolean -> toProtoBoolean(value)
            else -> throw IllegalArgumentException(
                "Unsupported value type: ${value!!::class.qualifiedName}"
            )
        }

        // 以下方法保持私有，只在 toProto 内部分派
        private fun toProtoInt(v: Int): BySelector = when (name) {
            "depth" -> bySelector { depth = v }
            "display_id" -> bySelector { displayId = v }
            else -> throw RuntimeException("unsupported property: $name")
        }

        private fun toProtoString(v: String): BySelector = when (name) {
            "clazz" -> bySelector {
                if (flags == null) {
                    clazz = v
                } else {
                    clazzPattern = regexPattern {
                        text = v
                        flags = flags
                    }
                }
            }

            "desc" -> bySelector {
                if (flags == null) {
                    desc = v
                } else {
                    descPattern = regexPattern {
                        text = v
                        flags = flags
                    }
                }
            }

            "pkg" -> bySelector {
                if (flags == null) {
                    pkg = v
                } else {
                    pkgPattern = regexPattern {
                        text = v
                        flags = flags
                    }
                }
            }

            "res" -> bySelector {
                if (flags == null) {
                    res = v
                } else {
                    resPattern = regexPattern {
                        text = v
                        flags = flags
                    }
                }
            }

            "text" -> bySelector {
                if (flags == null) {
                    text = v
                } else {
                    textPattern = regexPattern {
                        text = v
                        flags = flags
                    }
                }
            }

            "hint" -> bySelector {
                if (flags == null) {
                    hint = v
                } else {
                    hintPattern = regexPattern {
                        text = v
                        flags = flags
                    }
                }
            }
            else -> throw RuntimeException("unsupported property: $name")
        }

        private fun toProtoBoolean(v: Boolean): BySelector = when (name) {
            "checkable" -> bySelector { checkable = v }
            "checked" -> bySelector { checked = v }
            "clickable" -> bySelector { clickable = v }
            "enabled" -> bySelector { enabled = v }
            "focusable" -> bySelector { focusable = v }
            "focused" -> bySelector { focused = v }
            "long_clickable" -> bySelector { longClickable = v }
            "scrollable" -> bySelector { scrollable = v }
            "selected" -> bySelector { selected = v }
            else -> throw RuntimeException("unsupported property: $name")
        }
    }
}