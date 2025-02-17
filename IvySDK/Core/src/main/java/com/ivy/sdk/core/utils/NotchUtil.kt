package com.ivy.sdk.core.utils

import android.os.Build
import com.ivy.sdk.base.App
import com.ivy.sdk.base.utils.ActivityUtil

class NotchUtil {

    companion object {

        fun hasNotch(): Boolean {
            val activity = ActivityUtil.Instance.activity ?: return false
            var hasNotch = false
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val windowInsets = activity.window.decorView.rootWindowInsets
                    if (windowInsets != null) {
                        val displayCutout = windowInsets.displayCutout
                        if (displayCutout != null) {
                            val rects = displayCutout.boundingRects
                            if (rects != null && rects.size > 0) {
                                hasNotch = true
                                return true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }
            return hasNotch
        }
    }

}