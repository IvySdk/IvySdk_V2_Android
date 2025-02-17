package com.ivy.sdk.core.notch

import android.graphics.Rect
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import java.util.*

internal class PNotchScreenSupport : INotchScreenSupport {

    @RequiresApi(api = Build.VERSION_CODES.P)
    override fun hasNotchInScreen(window: Window): Boolean {
        val decorView = window.decorView
        val windowInsets = decorView.rootWindowInsets ?: return false
        val dct = windowInsets.displayCutout
        return dct != null && (dct.safeInsetTop != 0
            || dct.safeInsetBottom != 0
            || dct.safeInsetLeft != 0
            || dct.safeInsetRight != 0)
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    override fun getNotchSize(window: Window): List<Rect> {
        val result = ArrayList<Rect>()
        val decorView = window.decorView
        val windowInsets = decorView.rootWindowInsets ?: return result
        val dct = windowInsets.displayCutout
        if (dct != null) {
            result.addAll(dct.boundingRects)
        }
        return result
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    override fun setWindowLayoutAroundNotch(window: Window) {
        val attributes = window.attributes
        attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = attributes
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    override fun setWindowLayoutBlockNotch(window: Window) {
        val attributes = window.attributes
        attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        window.attributes = attributes
    }
}
