package com.ivy.sdk.admob

import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdProperty
import com.ivy.sdk.base.ads.AdProvider
import com.ivy.sdk.base.ads.AdType
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdProvider
import com.ivy.sdk.base.ads.IPAMManager
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.utils.ILog

open class AdmobAdProvider : IAdProvider() {

    override var pamManager: IPAMManager? = PAMManager()

    companion object {
        const val TAG = "Admob"
    }

    override fun setup(debug: Boolean, adConfig: AdConfig, adListener: IAdListener) {
        super.setup(debug, adConfig, adListener)
        val params = mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "01", EventParams.EVENT_PARAM_AD_NETWORK to AdProvider.ADMOB.value)
        adListener.logEvent(EventIDs.PLATFORM_INIT_START, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        Thread {
            if (debug) {
                MobileAds.setRequestConfiguration(
                    RequestConfiguration.Builder()
                        .setTestDeviceIds(
                            listOf(
                                "F3EDE78A2C3C4127A07CA5E97F0FDD02",
                                "F7866672B5B290E5948728B76E3CA555",
                                "BFAC108ACBCDABDA1C0CEDA8B7FE54E8"
                            )
                        )
                        .build()
                )
            }
            MobileAds.initialize(App.Instance) { status ->
                if (debug) {
                    MobileAds.openAdInspector(App.Instance) { err ->
                        if (err != null) {
                            ILog.e(TAG, "open Ad Inspector err:${err.message}")
                        }
                    }
                }
            }
        }.start()
        val params1 = mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "02", EventParams.EVENT_PARAM_AD_NETWORK to AdProvider.ADMOB.value)
        adListener.logEvent(EventIDs.PLATFORM_INIT_COMPLETED, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params1)
    }

    override fun addTask(adProperty: AdProperty) {
        when (adProperty.adType) {
            AdType.BANNER -> {
                BannerAdLoader(adProperty, adConfig, pamManager, adListener).apply {
                    bannerAdLoaderTasks[adProperty.adUnit] = this
                    this.loadAd()
                }
            }

            AdType.REWARDED -> {
                RewardedAdLoader(adProperty, adConfig, pamManager, adListener).apply {
                    rewardedAdLoaderTasks[adProperty.adUnit] = this
                    this.loadAd()
                }
            }

            AdType.INTERSTITIAL -> {
                InterstitialAdLoader(adProperty, adConfig, pamManager, adListener).apply {
                    interstitialAdLoaderTasks[adProperty.adUnit] = this
                    this.loadAd()
                }
            }

            else -> ILog.i(TAG, "unsupported ad type::${adProperty.adType.value}")
        }
    }


}