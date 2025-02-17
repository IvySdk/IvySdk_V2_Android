package com.ivy.sdk.base.utils

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.ivy.sdk.base.App
import com.ivy.sdk.base.storage.LocalStorage

object AppStoreUtil {

    /**
     * 每5次评价，触发一次应用内评价
     */
    fun rateUs(star: Int, debug: Boolean) {
        val requestCount = LocalStorage.Instance.decodeInt("_last_in_app_rate_", 0)
        if (requestCount % 5 != 0) {
            LocalStorage.Instance.encodeInt("_last_in_app_rate_", requestCount + 1)
            return
        }
        LocalStorage.Instance.encodeInt("_last_in_app_rate_", 1)
        val manager = if (debug) FakeReviewManager(App.Instance) else ReviewManagerFactory.create(App.Instance)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                ActivityUtil.Instance.activity?.let { activity ->
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }
                } ?: openAppStore(App.Instance.packageName, null)
            } else {
                // There was some problem, log or handle the error code.
                try {
                    val reviewErrorCode = (task.exception as ReviewException).errorCode
                    ILog.e("review", "show in-app review err:$reviewErrorCode")
                } catch (e: Exception) {
                }
                openAppStore(App.Instance.packageName, null)
            }
        }
    }

    fun openAppStore(url: String?, referrer: String?) {
        val path = url ?: App.Instance.packageName
        if (path.startsWith("market://details?id=", true)) {
            //app store url, do open
            val fixedUrl = referrer?.let {
                appendReferrer(App.Instance.packageName, path, referrer)
            } ?: path
            try {
                // 尝试打开商店
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl))
                ActivityUtil.Instance.activity?.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                //找不到商店,尝试网页打开
                val resetUrl = fixedUrl.replace("market://details?id=", "https://play.google.com/store/apps/details?id=", true)
                startBrowser(resetUrl)
            } catch (e: Exception) {
                // another exception; do nothing
            }
        } else if (path.startsWith("http", true)) {
            //link url
            val fixedUrl = referrer?.let {
                appendReferrer(App.Instance.packageName, path, referrer)
            } ?: path
            startBrowser(fixedUrl)
        } else {
            // do as it it a pkg name
            val marketUrl = "market://details?id=$path"
            openAppStore(marketUrl, referrer)
        }
    }

    private fun startBrowser(url: String) {
        IvyUtil.openUrlWithBrowser(url)
//        try {
//            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            App.Instance.activity?.startActivity(webIntent)
//        } catch (_: Exception) {
//
//        }
    }

    private fun appendReferrer(srcPkg: String, url: String, tag: String): String {
        if (!url.contains("&referrer")) {
            val sb = StringBuilder(url)
            sb.append("&referrer=utm_source%3D").append("ivy")
                .append("%26utm_campaign%3D").append(srcPkg)
                .append("%26utm_medium%3D").append(tag)
                .append("%26utm_term%3D").append(tag)
                .append("%26utm_content%3D").append(tag)
            return sb.toString()
        } else {
            return url
        }
    }


}