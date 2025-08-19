package io.appium.multiplatform.util

import android.annotation.SuppressLint
import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass
import org.lsposed.hiddenapibypass.LSPass
import java.lang.reflect.InvocationTargetException


/**
 * 通用 HiddenApi 工厂，用于反射调用 LSPass / HiddenApiBypass 等 Provider
 */
object HiddenApi {

    /**
     * Provider 接口，封装不同实现的 newInstance / invoke 方法
     */
    interface Delegate {
        @Throws(
            NoSuchMethodException::class,
            IllegalAccessException::class,
            InvocationTargetException::class,
            InstantiationException::class
        )
        fun newInstance(clazz: Class<*>, vararg args: Any?): Any?

        @Throws(
            NoSuchMethodException::class,
            IllegalAccessException::class,
            InvocationTargetException::class
        )
        fun invoke(clazz: Class<*>, obj: Any?, methodName: String, vararg args: Any?): Any?
    }

    /**
     * 枚举声明各 Provider，方便扩展和排序
     */

    enum class Provider(val delegate: Delegate) {
        LS_PASS(object : Delegate {
            init {
                checkApi28OrAbove()
            }

            override fun newInstance(clazz: Class<*>, vararg args: Any?): Any? =
                LSPass.newInstance(clazz, *args)

            override fun invoke(clazz: Class<*>, obj: Any?, methodName: String, vararg args: Any?): Any? =
                LSPass.invoke(clazz, obj, methodName, *args)
        }),
        HIDDEN_API_BYPASS(object : Delegate {
            init {
                checkApi28OrAbove()
            }

            override fun newInstance(clazz: Class<*>, vararg args: Any?): Any? =
                HiddenApiBypass.newInstance(clazz, *args)

            override fun invoke(clazz: Class<*>, obj: Any?, methodName: String, vararg args: Any?): Any? =
                HiddenApiBypass.invoke(clazz, obj, methodName, *args)
        })
    }

    /**
     * 可配置 Provider 顺序，默认顺序：LS_PASS -> HIDDEN_API_BYPASS
     *
     * 前者速度更快，测试io.appium.multiplatform.service.UiDeviceProvider.get由 380ms-> 360ms 提高 20ms。
     * 调整顺序：`HiddenApi.providers = listOf(HiddenApi.Provider.HIDDEN_API_BYPASS, HiddenApi.Provider.LS_PASS)`
     */
    var providers: List<Provider> = listOf(Provider.LS_PASS, Provider.HIDDEN_API_BYPASS)

    @SuppressLint("ObsoleteSdkInt")
    private fun checkApi28OrAbove() {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            "HiddenApiBypass needs API 28 or higher."
        }
    }

    /**
     * 根据 className 创建实例
     */
    @Throws(
        ClassNotFoundException::class,
        NoSuchMethodException::class,
        IllegalAccessException::class,
        InvocationTargetException::class,
        InstantiationException::class
    )
    fun newInstance(className: String, vararg args: Any?): Any {
        val clazz = Class.forName(className)
        return newInstance(clazz, *args)
    }

    /**
     * 根据 Class 对象创建实例，依次尝试 providers
     */
    @Throws(
        NoSuchMethodException::class,
        IllegalAccessException::class,
        InvocationTargetException::class,
        InstantiationException::class
    )
    fun newInstance(clazz: Class<*>, vararg args: Any?): Any {
        var lastEx: Throwable? = null
        for (provider in providers) {
            try {
                return provider.delegate.newInstance(clazz, *args)!!
            } catch (e: IllegalAccessException) {
                throw e // 遇到 IllegalAccessException 立即抛
            } catch (e: Throwable) {
                lastEx = e
            }
        }
        throw lastEx ?: NoSuchMethodException("No provider could create instance for ${clazz.name}")
    }

    /**
     * 调用 className 的静态方法或对象方法
     */
    @Throws(
        ClassNotFoundException::class,
        NoSuchMethodException::class,
        IllegalAccessException::class,
        InvocationTargetException::class
    )
    fun invoke(className: String, obj: Any?, methodName: String, vararg args: Any?): Any {
        val clazz = Class.forName(className)
        return invoke(clazz, obj, methodName, *args)
    }

    /**
     * 调用 Class 对象的静态方法或对象方法，依次尝试 providers
     */
    @Throws(
        NoSuchMethodException::class,
        IllegalAccessException::class,
        InvocationTargetException::class
    )
    fun invoke(clazz: Class<*>, obj: Any?, methodName: String, vararg args: Any?): Any {
        var lastEx: Throwable? = null
        for (provider in providers) {
            try {
                return provider.delegate.invoke(clazz, obj, methodName, *args)!!
            } catch (e: IllegalAccessException) {
                throw e // 遇到 IllegalAccessException 立即抛
            } catch (e: Throwable) {
                lastEx = e
            }
        }
        throw lastEx ?: NoSuchMethodException("No provider could invoke $methodName on ${clazz.name}")
    }
}