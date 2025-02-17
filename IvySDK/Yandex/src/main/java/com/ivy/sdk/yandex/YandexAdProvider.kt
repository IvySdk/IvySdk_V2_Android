package com.ivy.sdk.yandex

import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdProperty
import com.ivy.sdk.base.ads.AdProvider
import com.ivy.sdk.base.ads.AdType
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdProvider
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.utils.ILog
import com.yandex.mobile.ads.common.MobileAds

open class YandexAdProvider : IAdProvider() {

    companion object {
        const val TAG = "Yandex"
    }

    override fun setup(debug: Boolean, adConfig: AdConfig, adListener: IAdListener) {
        super.setup(debug, adConfig, adListener)
        ILog.i(TAG, "start init ad platform")
        val params =
            mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "01", EventParams.EVENT_PARAM_AD_NETWORK to AdProvider.YANDEX.value)
        adListener.logEvent(EventIDs.PLATFORM_INIT_START, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)

        MobileAds.initialize(App.Instance) {
            ILog.i(TAG, "init ad platform succeed")
            val params1 =
                mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "02", EventParams.EVENT_PARAM_AD_NETWORK to AdProvider.YANDEX.value)
            adListener.logEvent(EventIDs.PLATFORM_INIT_COMPLETED, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params1)
            if (debug){
                MobileAds.showDebugPanel(App.Instance)
            }
        }
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