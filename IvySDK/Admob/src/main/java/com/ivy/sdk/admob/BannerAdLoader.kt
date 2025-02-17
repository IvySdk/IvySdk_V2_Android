package com.ivy.sdk.admob

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdProperty
import com.ivy.sdk.base.ads.AdType
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdLoader
import com.ivy.sdk.base.ads.IPAMManager
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.utils.ActivityUtil
import com.ivy.sdk.base.utils.ILog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class BannerAdLoader(adProperty: AdProperty, adConfig: AdConfig, pamManager: IPAMManager?, adListener: IAdListener) :
    IAdLoader(adProperty, adConfig, pamManager, adListener),
    OnPaidEventListener {

    private var adView: AdView? = null

    private var lastRevenue: Double = 0.0

    override fun doLoadAd() {
        ActivityUtil.Instance.activity?.let { activity ->
//            adView?.destroy()
//            adView = null
            if (adView == null) {
                adView = AdView(activity).apply {
                    setAdSize(this@BannerAdLoader.getAdSize())
                    this.adUnitId = adProperty.adUnit
                    this.adListener = bannerAdListener
                }
            }

            val adRequest = AdRequest.Builder()
            var extra: Bundle? = null
            if (!adProperty.collapsible.isNullOrEmpty()) {
                extra = Bundle().apply { putString("collapsible", adProperty.collapsible) }
            }
            (pamManager?.getPAM(AdType.INTERSTITIAL, lastRevenue) as? PAM)?.let { pam ->
                extra = (extra ?: Bundle()).apply {
                    val pamBundle = Bundle().apply { putString("userGroup", pam.value) }
                    putBundle("admob_custom_keyvals", pamBundle)
                }
            }
            extra?.let { adRequest.addNetworkExtrasBundle(AdMobAdapter::class.java, it) }
            adView?.loadAd(adRequest.build())
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

    override fun onResume(container: FrameLayout?) {
        super.onResume(container)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                container?.getChildAt(0)?.let { view ->
                    if (view is AdView) {
                        view.resume()
                    }
                }
            } catch (_: Exception) {
            }
            }
    }

    override fun onPause(container: FrameLayout?) {
        super.onPause(container)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                container?.getChildAt(0)?.let { view ->
                    if (view is AdView) {
                        view.pause()
                    }
                }
            } catch (_: Exception) {
            }
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

    override fun isReady(): Boolean = adView != null && isBannerReady

    override fun onPaidEvent(adValue: AdValue) {
        ILog.i(TAG, "${adProperty.adType.value} paid event:")
        ILog.i(TAG, "ad unit:${adProperty.adUnit}")
        ILog.i(TAG, "value:${adValue.valueMicros};currency:${adValue.currencyCode}")

        val revenue: Double = (adValue.valueMicros / 1000000.0f).toDouble()
        if (revenue >= 10.0 || revenue <= 0) {
            return
        }
        lastRevenue = revenue
        onAdRevenuePaid(revenue, adValue.currencyCode)
    }

    private fun getAdSize(): AdSize = when (adProperty.adaptive) {
        true -> {
            val metrics = DisplayMetrics()
            val windowManager = (App.Instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            windowManager.defaultDisplay.getMetrics(metrics)
            val dpWidth = (metrics.widthPixels / metrics.density).toInt()
            AdSize.getInlineAdaptiveBannerAdSize(dpWidth, 60)
        }

        false -> AdSize.BANNER
    }

    private val bannerAdListener = object : AdListener() {
        override fun onAdClicked() {
            super.onAdClicked()
            this@BannerAdLoader.onAdClicked()
        }

        override fun onAdClosed() {
            super.onAdClosed()
            this@BannerAdLoader.onAdClosed()
        }

        override fun onAdImpression() {
            super.onAdImpression()
            onAdShowSuccess()
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            super.onAdFailedToLoad(p0)
            adView = null
            onAdLoadFailure("${p0.code};${p0.message}")
        }

        override fun onAdLoaded() {
            super.onAdLoaded()
            try {
                adView?.responseInfo?.loadedAdapterResponseInfo?.let { resp ->
                    eventParams[EventParams.EVENT_PARAM_AD_MEDIATION] = resp.adSourceName
                    eventParams[EventParams.EVENT_PARAM_AD_SOURCE_INSTANCE] = resp.adSourceInstanceName
                }

                val extra = adView?.responseInfo?.responseExtras
                eventParams[EventParams.EVENT_PARAM_AD_MEDIATION_GROUP] = extra?.getString("mediation_group_name", "") ?: ""
                eventParams[EventParams.EVENT_PARAM_AD_MEDIATION_AB_TEST] = extra?.getString("mediation_ab_test_name", "") ?: ""
            } catch (_: Exception) {

            }
            adView
            onAdLoadSuccess()
        }
    }

}