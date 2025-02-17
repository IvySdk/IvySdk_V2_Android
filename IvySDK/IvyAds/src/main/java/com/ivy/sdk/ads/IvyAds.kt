package com.ivy.sdk.ads

import android.app.Activity
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdType
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdProvider
import com.ivy.sdk.base.remote.IRCManager
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.TrackPlatform
import com.ivy.sdk.base.utils.ActivityUtil
import com.ivy.sdk.base.utils.ILog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

open class IvyAds private constructor() {

    companion object {
        const val TAG = "ads"
        val Instance by lazy(LazyThreadSafetyMode.NONE) { IvyAds() }
    }

    private var debug: Boolean = false
    private var adConfig: AdConfig = AdConfig.decode(null)

    private var pamData: String? = null

    private var adListener: IAdListener? = null
    private var ivyBannerAd: IvyBaseAd? = null
    private var ivyInterstitialAd: IvyBaseAd? = null
    private var ivyRewardedAd: IvyBaseAd? = null

    private val bannerAdRoller: BannerAdRoller = BannerAdRoller(object : IBannerAdRollerHelper {
        override fun getIvyBannerAd(): IvyBaseAd? = ivyBannerAd
    })

    private val adProviders: MutableMap<String, IAdProvider> = mutableMapOf()

    private val innerAdListener = object : IAdListener() {
        override fun onAdLoadSuccess(adType: AdType) {
            adListener?.onAdLoadSuccess(adType)
            if (adType == AdType.BANNER) {
                bannerAdRoller.onBannerAdLoaded()
            }
        }

        override fun onAdLoadFailure(adType: AdType, reason: String?) {
            adListener?.onAdLoadFailure(adType, reason)
        }

        override fun onAdShowSuccess(adType: AdType, tag: String, placement: Int) {
            adListener?.onAdShowSuccess(adType, tag, placement)
        }

        override fun onAdShowFailed(adType: AdType, reason: String?, tag: String, placement: Int) {
            adListener?.onAdShowFailed(adType, reason, tag, placement)
        }

        override fun onAdClicked(adType: AdType, tag: String, placement: Int) {
            adListener?.onAdClicked(adType, tag, placement)
        }

        override fun onAdClosed(adType: AdType, gotReward: Boolean, tag: String, placement: Int) {
            adListener?.onAdClosed(adType, gotReward, tag, placement)
        }

        override fun logEvent(
            eventName: String, eventType: String, eventSrc: String,
            params: MutableMap<String, Any>?, platforms: List<TrackPlatform>?
        ) {
            super.logEvent(eventName, eventType, eventSrc, params, platforms)
            adListener?.logEvent(eventName, eventType, eventSrc, params, platforms)
        }

    }

    fun setup(config: String, debug: Boolean, rcManager: IRCManager, adListener: IAdListener) {
        this.debug = debug
        this.adListener = adListener
        CoroutineScope(Dispatchers.Default).launch {
            while (ActivityUtil.Instance.activity == null) {
                ILog.i(TAG, "current activity is null")
                delay(300)
            }
            initAdConfig(ActivityUtil.Instance.activity!!, config, rcManager, innerAdListener)
        }
    }

    private fun initAdConfig(activity: Activity, config: String, rcManager: IRCManager, adListener: IAdListener) {
        try {
            val parent = JSONObject(config)
            adConfig = AdConfig.decode(parent.optJSONObject("adConfig"))
            ILog.i(TAG, "start check Admob UMP")
            AdsConsentManager.Instance.checkGoogleUMP(activity, debug) {
                ILog.i(TAG, "Admob UMP checked!!!")
                pamData = rcManager.getString("pam") ?: parent.optString("pam")
                try {
                    parent.optJSONArray("video")?.let {
                        ivyRewardedAd = IvyRewardedAd()
                        ivyRewardedAd?.setup(debug, it, adConfig, iBaseAdHelper, adListener)
                        ILog.i(TAG, "rewarded ad config:$it")
                    }
                } catch (e: Exception) {
                    ILog.e(TAG, "init rewarded ad failed:${e.message}")
                }
                try {
                    parent.optJSONArray("full")?.let {
                        ivyInterstitialAd = IvyInterstitialAd()
                        ivyInterstitialAd?.setup(debug, it, adConfig, iBaseAdHelper, adListener)
                        ILog.i(TAG, "interstitial ad config:$it")
                    }
                } catch (e: Exception) {
                    ILog.e(TAG, "init interstitial ad failed:${e.message}")
                }
                try {
                    parent.optJSONArray("banner")?.let {
                        ivyBannerAd = IvyBannerAd()
                        ivyBannerAd?.setup(debug, it, adConfig, iBaseAdHelper, adListener)
                        ILog.i(TAG, "banner ad config:$it")
                    }
                } catch (e: Exception) {
                    ILog.e(TAG, "init banner ad failed:${e.message}")
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "init ads failed:${e.message}")
        }
    }

    private val iBaseAdHelper = object : IBaseAdHelper {
        override fun getAdProvider(adProvider: String, autoInstantiate: Boolean): IAdProvider? {
            var provider = adProviders[adProvider]
            if (provider == null && autoInstantiate) {
                //创建adProvider
                provider = AdProviderFactory.instantiate(adProvider)
                if (provider != null) {
                    provider.setup(debug, adConfig, innerAdListener)
                    provider.getPAMManager()?.setupPAMData(pamData)
                    adProviders[adProvider] = provider
                }
            }
            return provider
        }
    }

    fun isAvailable(adType: AdType): Boolean = when (adType) {
        AdType.BANNER -> ivyBannerAd?.isReady() ?: false
        AdType.NATIVE -> false
        AdType.SPLASH -> false
        AdType.REWARDED -> ivyRewardedAd?.isReady() ?: false
        AdType.INTERSTITIAL -> ivyInterstitialAd?.isReady() ?: false
    }

    fun showAd(adType: AdType, tag: String, placement: Int, clientInfo: String?) {
        val activity = ActivityUtil.Instance.activity
        if (activity == null) {
            adListener?.onAdShowFailed(adType, "invalid activity", tag, placement)
            return
        }
        when (adType) {
            AdType.BANNER -> {}
            AdType.NATIVE -> {}
            AdType.SPLASH -> {}
            AdType.REWARDED -> ivyRewardedAd?.showAd(activity, tag, placement, clientInfo) ?: run {
                ILog.e(TAG, "${adType.value} show failed!invalid ad provider")
                adListener?.onAdShowFailed(adType, "invalid ad provider", tag, placement)
                adListener?.logEvent(
                    EventIDs.AD_SHOW_FAILED,
                    EventType.EVENT_TYPE_COMMON,
                    EventSrc.EVENT_SRC_SDK,
                    mutableMapOf("ad_format" to AdType.REWARDED.value, EventParams.EVENT_PARAM_FAIL_REASON to "not_ready")
                )
            }

            AdType.INTERSTITIAL -> ivyInterstitialAd?.showAd(activity, tag, placement, clientInfo) ?: run {
                ILog.e(TAG, "${adType.value} show failed!invalid ad provider")
                adListener?.onAdShowFailed(adType, "invalid ad provider", tag, placement)
                adListener?.logEvent(
                    EventIDs.AD_SHOW_FAILED,
                    EventType.EVENT_TYPE_COMMON,
                    EventSrc.EVENT_SRC_SDK,
                    mutableMapOf("ad_format" to AdType.INTERSTITIAL.value, EventParams.EVENT_PARAM_FAIL_REASON to "not_ready")
                )
            }
        }
    }

    fun showBannerAd(position: Int, tag: String, placement: Int, clientInfo: String?) {
        val task = BannerAdRollerTask(position, tag, placement, clientInfo, adConfig, debug)
        bannerAdRoller.addRollerTask(task)
        if (!bannerAdRoller.isRolling()) {
            bannerAdRoller.startRoller()
        }
    }

    fun closeBannerAd(placement: Int){
        bannerAdRoller.removeRollerTask("$placement")?.let {
            ivyBannerAd?.closeBannerAd(it, placement)
        }
    }

    fun onResume() {
        bannerAdRoller.onResume()
    }

    fun onPause() {
        bannerAdRoller.onPause()
    }

    fun onDestroy(){
        bannerAdRoller.stopRoller()
        bannerAdRoller.onDestroy()
    }


}