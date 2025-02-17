package com.ivy.sdk.base.utils

import android.app.Activity
import java.lang.ref.WeakReference

open class ActivityUtil private constructor() {

    companion object {
        val Instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ActivityUtil() }
    }

    private var activityInstance: WeakReference<Activity>? = null

    var activity: Activity?
        get() = activityInstance?.get()
        set(value) {
            activityInstance = WeakReference(value)
        }


}