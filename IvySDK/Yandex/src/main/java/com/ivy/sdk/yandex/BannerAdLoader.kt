package com.ivy.sdk.yandex

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdProperty
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdLoader
import com.ivy.sdk.base.ads.IPAMManager
import com.ivy.sdk.base.utils.ActivityUtil
import com.ivy.sdk.base.utils.ILog
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import org.json.JSONObject

internal class BannerAdLoader(adProperty: AdProperty, adConfig: AdConfig, pamManager: IPAMManager?, adListener: IAdListener) : IAdLoader(
    adProperty, adConfig, pamManager, adListener
) {

    private var adView: BannerAdView? = null

    override fun isReady(): Boolean = adView != null && isBannerReady

    override fun doLoadAd() {
        ActivityUtil.Instance.activity?.let { activity ->
            if (adView == null) {
                adView = BannerAdView(activity).apply {
                    val adSize = BannerAdSize.stickySize(App.Instance, App.Instance.resources.displayMetrics.widthPixels)
                    setAdSize(adSize)
                    setAdUnitId(adProperty.adUnit)
                    setBannerAdEventListener(adEventListener)
                }
            }
            val adRequest = AdRequest.Builder().build()
            adView?.loadAd(adRequest)
        } ?: onAdLoadFailure("invalid activity")
    }

    override fun getBannerAdView(): View? = adView

    override fun showBannerAd(container: FrameLayout, tag: String, placement: Int, clientInfo: String?): Boolean {
        super.showBannerAd(container, tag, placement, clientInfo)
        if (isReady() && adView != null) {
            try {
                (adView?.parent as? ViewGroup)?.removeView(adView)
                container.addView(adView)
                return true
            } catch (e: Exception) {
                ILog.e(TAG, "banner show failed:${e.message}")
            }
        }
        return false
    }

    private val adEventListener = object : BannerAdEventListener {
        override fun onAdClicked() {
            this@BannerAdLoader.onAdClicked()
        }

        override fun onAdFailedToLoad(error: AdRequestError) {
            onAdLoadFailure("${error.code};${error.description}")
        }

        override fun onAdLoaded() {
            onAdLoadSuccess()
        }

        override fun onImpression(impressionData: ImpressionData?) {
            onAdShowSuccess()
            impressionData?.rawData?.let { data ->
                try {
                    val json = JSONObject(data)
                    val revenue: Double = json.getDouble("revenueUSD")
                    if (revenue >= 10.0 || revenue <= 0) {
                        return
                    }
                    onAdRevenuePaid(revenue, "USD")
                } catch (_: Exception) {
                }
            }
        }

        override fun onLeftApplication() {

        }

        override fun onReturnedToApplication() {

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (adView?.parent != null) {
                (adView?.parent as FrameLayout).removeAllViews()
            }
            adView?.destroy()
            adView = null
        } catch (_: Exception) {
        }
    }


}