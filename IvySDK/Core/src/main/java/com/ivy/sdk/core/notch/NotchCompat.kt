package com.ivy.sdk.core.notch

import android.graphics.Rect
import android.os.Build
import android.view.Window

/**
 * 凹口屏适配工具类
 * 默认Android O以上才有刘海屏,Android P及以上调用官方API判断,Android O利用厂商API判断
 */
object NotchCompat {
    private var mNotchScreenSupport: INotchScreenSupport? = null

    /**
     * 判断屏幕是否为凹口屏
     * WindowInsets在View Attach到Window上之后才会创建
     * 因此想要获得正确的结果，方法的调用时机应在DecorView Attach之后
     */
    fun hasDisplayCutout(window: Window): Boolean {
        checkScreenSupportInit()
        return mNotchScreenSupport!!.hasNotchInScreen(window)
    }

    fun hasDisplayCutoutHardware(window: Window): Boolean {
        checkScreenSupportInit()
        return mNotchScreenSupport!!.hasNotchInScreenHardware(window)
    }

    /**
     * 获取凹口屏大小
     */
    fun getDisplayCutoutSize(window: Window): List<Rect> {
        checkScreenSupportInit()
        return mNotchScreenSupport!!.getNotchSize(window)
    }

    fun getDisplayCutoutSizeHardware(window: Window): List<Rect> {
        checkScreenSupportInit()
        return mNotchScreenSupport!!.getNotchSizeHardware(window)
    }

    /**
     * 设置始终使用凹口屏区域
     */
    fun immersiveDisplayCutout(window: Window) {
        checkScreenSupportInit()
        mNotchScreenSupport!!.setWindowLayoutAroundNotch(window)
    }

    /**
     * 设置始终不使用凹口屏区域
     */
    fun blockDisplayCutout(window: Window) {
        checkScreenSupportInit()
        mNotchScreenSupport!!.setWindowLayoutBlockNotch(window)
    }

    private fun checkScreenSupportInit() {
        if (mNotchScreenSupport != null) return
        mNotchScreenSupport = when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> DefaultNotchScreenSupport()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> PNotchScreenSupport()
            RomUtils.isMiuiRom -> MiNotchScreenSupport()
            RomUtils.isHuaweiRom -> HwNotchScreenSupport()
            RomUtils.isOppoRom -> OppoNotchScreenSupport()
            RomUtils.isVivoRom -> VivoNotchScreenSupport()
            RomUtils.isMeizuRom -> MeizuNotchScreenSupport()
            RomUtils.isSamsungRom -> SamsungNotchScreenSupport()
            else -> DefaultNotchScreenSupport()
        }
    }
}
