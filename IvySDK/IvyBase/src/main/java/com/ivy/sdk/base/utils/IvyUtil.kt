package com.ivy.sdk.base.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.view.Display
import com.ivy.sdk.base.App
import java.io.File

class IvyUtil {

    companion object {
        const val TAG = "IvyUtil"

        fun systemShareText(activity: Activity, txt: String) {
            try {
                Intent().apply {
                    setAction(Intent.ACTION_SEND)
                    putExtra(Intent.EXTRA_TEXT, txt)
                    setType("text/plain")
                    activity.startActivity(this)
                }
            } catch (e: Exception) {
                ILog.e(TAG, "system share txt failed:${e.message}")
            }
        }

        fun systemShareImage(title: String, imagePath: String) {
            try {
                val imgFile = File(imagePath)
                if (imgFile.exists()) {
                    val shareIntent = Intent().apply {
                        setAction(Intent.ACTION_SEND)
                        putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imgFile))
                        setType("image/*")
                    }
                    val shareChooser = Intent.createChooser(shareIntent, title)
                    if (shareIntent.resolveActivity(App.Instance.packageManager) != null) {
                        ActivityUtil.Instance.activity?.startActivity(shareChooser)
                    } else {
                        throw Exception("unable open share activity")
                    }
                } else {
                    throw IllegalArgumentException("invalid img path")
                }
            } catch (e: Exception) {
                ILog.e(TAG, "system share img failed:${e.message}")
            }
        }

        fun openUrlWithBrowser(url: String?) {
            url?.let { netUrl ->
                try {
                    val intent = Intent().apply {
                        setAction(Intent.ACTION_VIEW)
                        setData(Uri.parse(netUrl))
                        App.Instance.packageManager.resolveActivity(this, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.let {
                            setPackage(it.packageName)
                        }
                    }
                    ActivityUtil.Instance.activity?.startActivity(intent) ?: ILog.e(TAG, "invalid activity to open browser")
                } catch (e: Exception) {
                    ILog.e(TAG, "open browser for $url failed;${e.message}")
                }
            } ?: ILog.e(TAG, "invalid url to open with browser")
        }

        fun loadDeviceInfo(): MutableMap<String, Any>? {
            try {
                val map = mutableMapOf<String, Any>()
                map["Brand"] = Build.BRAND
                map["Board"] = Build.BOARD
                map["SDK"] = Build.VERSION.SDK_INT
                map["Version"] = Build.VERSION.RELEASE
                map["Manufacturer"] = Build.MANUFACTURER
                map["ABI"] = Build.SUPPORTED_ABIS[0]
                map["Cores"] = Runtime.getRuntime().availableProcessors()
                val displayManager: DisplayManager = App.Instance.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
                map["refreshRate"] = display.refreshRate
                val displayMetrics = App.Instance.resources.displayMetrics
                map["density"] = displayMetrics.density
                map["densityDpi"] = displayMetrics.densityDpi
                map["widthPixels"] = displayMetrics.widthPixels
                map["heightPixels"] = displayMetrics.heightPixels
                map["TotalMemory"] = Util.byte2MB(Util.getTotalMemory())
                map["InternalTotalSpace"] = Util.byte2MB(Util.getTotalDiskSize())
                return map
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

    }

}