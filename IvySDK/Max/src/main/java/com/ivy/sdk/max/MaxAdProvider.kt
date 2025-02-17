package com.ivy.sdk.max

import android.content.pm.PackageManager
import com.amazon.device.ads.AdRegistration
import com.amazon.device.ads.MRAIDPolicy
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
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
import com.ivy.sdk.base.utils.ActivityUtil
import com.ivy.sdk.base.utils.ILog

open class MaxAdProvider : IAdProvider() {

    companion object {
        const val TAG = "MAX"
    }

    override fun setup(debug: Boolean, adConfig: AdConfig, adListener: IAdListener) {
        super.setup(debug, adConfig, adListener)
        ILog.i(TAG, "start init ad platform")
        val params = mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "01", EventParams.EVENT_PARAM_AD_NETWORK to AdProvider.APPLOVIN_MAX.value)
        adListener.logEvent(EventIDs.PLATFORM_INIT_START, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        val applicationInfo = App.Instance.packageManager.getApplicationInfo(App.Instance.packageName, PackageManager.GET_META_DATA)
        try {
            applicationInfo.metaData.getString("aps.id")?.let { apsAppId ->
                if (apsAppId.isBlank()){
                    throw IllegalArgumentException("invalid aps app id")
                }
                ActivityUtil.Instance.activity?.let { activity ->
                    AdRegistration.getInstance(apsAppId, activity)
                    AdRegistration.setMRAIDSupportedVersions(arrayOf("1.0", "2.0", "3.0"))
                    AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM)

                    /** amazon ad 测试  */
                    if (debug) {
                        AdRegistration.enableTesting(true)
                        AdRegistration.enableLogging(true)
                    }
                }
            }
        } catch (_: Exception) {
        }
        applicationInfo.metaData.getString("sdk.applovin.sdk.key")?.let { key ->
            val initConfig = AppLovinSdkInitializationConfiguration.builder(key, App.Instance)
                .setMediationProvider(AppLovinMediationProvider.MAX)
                .build()
            AppLovinSdk.getInstance(App.Instance).settings.setVerboseLogging(debug)
            AppLovinSdk.getInstance(App.Instance).settings.isCreativeDebuggerEnabled = debug
            AppLovinSdk.getInstance(App.Instance).initialize(initConfig) {
                if (debug) {
                    AppLovinSdk.getInstance(App.Instance).showMediationDebugger()
                }
                ILog.i(TAG, "init ad platform succeed")
                val params1 = mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "02", EventParams.EVENT_PARAM_AD_NETWORK to AdProvider.APPLOVIN_MAX.value)
                adListener.logEvent(EventIDs.PLATFORM_INIT_COMPLETED, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params1)
            }
        } ?: ILog.e(TAG, "init ad platform failed!!! invalid applovin sdk key")
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