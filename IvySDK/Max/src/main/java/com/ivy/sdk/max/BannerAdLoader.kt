package com.ivy.sdk.max

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.amazon.aps.ads.model.ApsAdNetwork
import com.amazon.device.ads.AdError
import com.amazon.device.ads.DTBAdCallback
import com.amazon.device.ads.DTBAdNetworkInfo
import com.amazon.device.ads.DTBAdRequest
import com.amazon.device.ads.DTBAdResponse
import com.amazon.device.ads.DTBAdSize
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxNetworkResponseInfo
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdkUtils
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdProperty
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdLoader
import com.ivy.sdk.base.ads.IPAMManager
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.utils.ILog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class BannerAdLoader(adProperty: AdProperty, adConfig: AdConfig, pamManager: IPAMManager?, adListener: IAdListener) :
    IAdLoader(adProperty, adConfig, pamManager, adListener),
    MaxAdViewAdListener, MaxAdRevenueListener {

    private var adView: MaxAdView? = null

    override fun doLoadAd() {
        if (adView == null) {
            adView = MaxAdView(adProperty.adUnit, App.Instance).apply {
                if (adProperty.adaptive) {
                    // Get the adaptive banner height.
                    val heightDp = MaxAdFormat.BANNER.getAdaptiveSize(App.Instance).height
                    val heightPx = AppLovinSdkUtils.dpToPx(App.Instance, heightDp)
                    val width = ViewGroup.LayoutParams.MATCH_PARENT
                    setLayoutParams(FrameLayout.LayoutParams(width, heightPx))
                    setExtraParameter("adaptive_banner", "true")
                }
                setExtraParameter("allow_pause_auto_refresh_immediately", "true")
                setListener(this@BannerAdLoader)
                setRevenueListener(this@BannerAdLoader)
            }
        }

        if (adProperty.apsAdUnit.isNotBlank()) {
            val adFormat: MaxAdFormat = if (AppLovinSdkUtils.isTablet(App.Instance)) MaxAdFormat.LEADER else MaxAdFormat.BANNER
            // Raw size will be 320x50 for BANNERs on phones, and 728x90 for LEADERs on tablets
            val rawSize = adFormat.size
            val size = DTBAdSize(rawSize.width, rawSize.height, adProperty.apsAdUnit)
            val adLoader = DTBAdRequest(App.Instance, DTBAdNetworkInfo(ApsAdNetwork.MAX))
            adLoader.setSizes(size)
            adLoader.loadAd(object : DTBAdCallback {
                override fun onFailure(p0: AdError) {
                    adView?.setLocalExtraParameter("amazon_ad_error", p0)
                    adView?.loadAd()
                    adView?.stopAutoRefresh()
                }

                override fun onSuccess(p0: DTBAdResponse) {
                    adView?.setLocalExtraParameter("amazon_ad_response", p0)
                    adView?.loadAd()
                    adView?.stopAutoRefresh()
                }
            })
        }
    }

    override fun getBannerAdView(): View? = adView

    override fun showBannerAd(container: FrameLayout, tag: String, placement: Int, clientInfo: String?): Boolean {
        super.showBannerAd(container, tag, placement, clientInfo)
        if (isReady() && adView != null) {
            try {
                (adView?.parent as? ViewGroup)?.removeView(adView)
                container.addView(adView)
                if (adConfig.bannerRefreshByPlatform) {
                    adView?.startAutoRefresh()
                }
                return true
            } catch (e: Exception) {
                ILog.e(TAG, "banner show failed:${e.message}")
            } finally {

            }
        }
        return false
    }

    override fun onResume(container: FrameLayout?) {
        super.onResume(container)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                container?.getChildAt(0)?.let { view ->
                    if (view is MaxAdView) {
                        if (adConfig.bannerRefreshByPlatform) {
                            view.startAutoRefresh()
                        }
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
                    if (view is MaxAdView) {
                        view.setExtraParameter("allow_pause_auto_refresh_immediately", "true")
                        view.stopAutoRefresh()
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

    override fun onAdLoaded(p0: MaxAd) {
        try {
            val waterfall = p0.waterfall.networkResponses
            var mediationNetWork: String? = null
            for (it in waterfall) {
                if (it.adLoadState == MaxNetworkResponseInfo.AdLoadState.AD_LOADED) {
                    mediationNetWork = it.mediatedNetwork.name
                    break
                }
            }
            mediationNetWork?.let { eventParams[EventParams.EVENT_PARAM_AD_MEDIATION] = it }

        } catch (_: Exception) {
        }
        onAdLoadSuccess()
    }

    override fun onAdDisplayed(p0: MaxAd) {
        ILog.i(TAG, "banner shown")
        onAdShowSuccess()
    }

    override fun onAdHidden(p0: MaxAd) {
        ILog.i(TAG, "banner closed")
        onAdClosed()
    }

    override fun onAdClicked(p0: MaxAd) {
        ILog.i(TAG, "banner clicked")
        onAdClicked()
    }

    override fun onAdLoadFailed(p0: String, p1: MaxError) {
        onAdLoadFailure("$p0;${p1.code};${p1.message}")
    }

    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
        onAdShowFailed("$p0;${p1.code};${p1.message}")
    }

    override fun onAdExpanded(p0: MaxAd) {

    }

    override fun onAdCollapsed(p0: MaxAd) {

    }

    override fun onAdRevenuePaid(ad: MaxAd) {
        try {
            val revenue = ad.revenue
            if (revenue >= 10.0 || revenue <= 0) {
                return
            }
            onAdRevenuePaid(revenue, "USD")
        } catch (_: Exception) {
        }
    }

//    private fun initAps() {
//        if (AdRegistration.isInitialized()) {
//            return
//        }
//        val applicationInfo = App.Instance.packageManager.getApplicationInfo(App.Instance.packageName, PackageManager.GET_META_DATA)
//        try {
//            applicationInfo.metaData.getString("aps.id")?.let { apsAppId ->
//                if (apsAppId.isBlank()) {
//                    throw IllegalArgumentException("invalid aps app id")
//                }
//                ActivityUtil.Instance.activity?.let { activity ->
//                    AdRegistration.getInstance(apsAppId, activity)
//                    AdRegistration.setMRAIDSupportedVersions(arrayOf("1.0", "2.0", "3.0"))
//                    AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM)
//
//                    /** amazon ad 测试  */
//                    val debug = applicationInfo.metaData.getBoolean("ivy.debug", false)
//                    if (debug) {
//                        AdRegistration.enableTesting(true)
//                        AdRegistration.enableLogging(true)
//                    }
//                }
//            }
//        } catch (_: Exception) {
//        }
//    }


}