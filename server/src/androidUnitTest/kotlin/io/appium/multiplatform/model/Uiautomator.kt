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
                        StringProperty("clazz", flags = null),
                        StringProperty("clazz", flags = 0),
                        StringProperty("clazz", flags = 0x32)
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
                        StringProperty("desc", flags = null),
                        StringProperty("desc", flags = 0),
                        StringProperty("desc", flags = 0x32)
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
                        StringProperty("pkg", flags = null),
                        StringProperty("pkg", flags = 0),
                        StringProperty("pkg", flags = 0x32)
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
                        StringProperty("res", flags = null),
                        StringProperty("res", flags = 0),
                        StringProperty("res", flags = 0x32)
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
                        StringProperty("text", flags = null),
                        StringProperty("text", flags = 0),
                        StringProperty("text", flags = 0x32)
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
                        StringProperty("hint", flags = null),
                        StringProperty("hint", flags = 0),
                        StringProperty("hint", flags = 0x32)
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
                    BooleanProperty("checkable", true),
                    BooleanProperty("checkable", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mCheckable")
                    (reflectField(by::class.java, "mCheckable").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    BooleanProperty("checked", true),
                    BooleanProperty("checked", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mChecked")
                    (reflectField(by::class.java, "mChecked").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    BooleanProperty("clickable", true),
                    BooleanProperty("clickable", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mClickable")
                    (reflectField(by::class.java, "mClickable").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    BooleanProperty("enabled", true),
                    BooleanProperty("enabled", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mEnabled")
                    (reflectField(by::class.java, "mEnabled").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    BooleanProperty("focusable", true),
                    BooleanProperty("focusable", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mFocusable")
                    (reflectField(by::class.java, "mFocusable").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    BooleanProperty("focused", true),
                    BooleanProperty("focused", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mFocused")
                    (reflectField(by::class.java, "mFocused").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    BooleanProperty("long_clickable", true),
                    BooleanProperty("long_clickable", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mLongClickable")
                    (reflectField(by::class.java, "mLongClickable").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    BooleanProperty("scrollable", true),
                    BooleanProperty("scrollable", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mScrollable")
                    (reflectField(by::class.java, "mScrollable").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    BooleanProperty("selected", true),
                    BooleanProperty("selected", false)
                ) { input ->
                    val by = input.toProto().asUiAutomator()
                    logger.info { "$input, $by" }
                    by::class.shouldHaveMemberProperty("mSelected")
                    (reflectField(by::class.java, "mSelected").get(by) as Boolean)
                        .shouldBe(input.value)
                        .shouldNotBeNull()
                }
                withData(
                    IntProperty("depth", 0),
                    IntProperty("depth", 1),
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
                    IntProperty("display_id", 0),
                    IntProperty("display_id", 1),
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
                            StringProperty("hint", flags = null),
                            StringProperty("hint", flags = 0),
                            StringProperty("hint", flags = 0x32)
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
                        IntProperty("display_id", 0),
                        IntProperty("display_id", 1),
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

    data class StringProperty(val name: String, val value: String = Uuid.random().toString(), val flags: Int?) :
        WithDataTestName {
        override fun dataTestName(): String = "[$name]-$value"
        fun toProto(): BySelector = when (name) {
            "clazz" -> bySelector {
                if (flags == null) {
                    clazz = value
                } else {
                    clazzPattern = regexPattern {
                        text = value
                        flags = flags
                    }
                }
            }

            "desc" -> bySelector {
                if (flags == null) {
                    desc = value
                } else {
                    descPattern = regexPattern {
                        text = value
                        flags = flags
                    }
                }
            }

            "pkg" -> bySelector {
                if (flags == null) {
                    pkg = value
                } else {
                    pkgPattern = regexPattern {
                        text = value
                        flags = flags
                    }
                }
            }

            "res" -> bySelector {
                if (flags == null) {
                    res = value
                } else {
                    resPattern = regexPattern {
                        text = value
                        flags = flags
                    }
                }
            }

            "text" -> bySelector {
                if (flags == null) {
                    text = value
                } else {
                    textPattern = regexPattern {
                        text = value
                        flags = flags
                    }
                }
            }

            "hint" -> bySelector {
                if (flags == null) {
                    hint = value
                } else {
                    hintPattern = regexPattern {
                        text = value
                        flags = flags
                    }
                }
            }

            else -> throw RuntimeException("unsupported property: $name")
        }
    }

    data class BooleanProperty(val name: String, val value: Boolean) : WithDataTestName {
        override fun dataTestName(): String = "[$name]-$value"
        fun toProto(): BySelector = when (name) {
            "checkable" -> bySelector { checkable = value }
            "checked" -> bySelector { checked = value }
            "clickable" -> bySelector { clickable = value }
            "enabled" -> bySelector { enabled = value }
            "focusable" -> bySelector { focusable = value }
            "focused" -> bySelector { focused = value }
            "long_clickable" -> bySelector { longClickable = value }
            "scrollable" -> bySelector { scrollable = value }
            "selected" -> bySelector { selected = value }
            else -> throw RuntimeException("unsupported property: $name")
        }
    }

    data class IntProperty(val name: String, val value: Int) : WithDataTestName {
        override fun dataTestName(): String = "[$name]-$value"
        fun toProto(): BySelector = when (name) {
            "depth" -> bySelector { depth = value }
            "display_id" -> bySelector { displayId = value }
            else -> throw RuntimeException("unsupported property: $name")
        }
    }
}