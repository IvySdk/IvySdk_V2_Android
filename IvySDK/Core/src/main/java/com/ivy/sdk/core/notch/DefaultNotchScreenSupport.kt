package com.ivy.sdk.core.notch

import android.graphics.Rect
import android.view.Window
import java.util.*

internal class DefaultNotchScreenSupport : INotchScreenSupport {
    override fun hasNotchInScreen(window: Window): Boolean {
        return false
    }

    override fun getNotchSize(window: Window): List<Rect> {
        return ArrayList()
    }

    override fun setWindowLayoutAroundNotch(window: Window) {}

    override fun setWindowLayoutBlockNotch(window: Window) {}
}
