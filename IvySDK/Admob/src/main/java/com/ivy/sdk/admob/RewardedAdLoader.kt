package com.ivy.sdk.admob

import android.app.Activity
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdProperty
import com.ivy.sdk.base.ads.AdType
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdLoader
import com.ivy.sdk.base.ads.IPAMManager
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.utils.ILog

internal class RewardedAdLoader(adProperty: AdProperty, adConfig: AdConfig, pamManager: IPAMManager?, adListener: IAdListener) :
    IAdLoader(adProperty, adConfig, pamManager, adListener),
    OnPaidEventListener {

    private var rewardedAd: RewardedAd? = null
    private var gotReward = false
    private var taskId: Int = 0

    private var lastRevenue: Double = 0.0

    override fun doLoadAd() {
        try {
            val adRequest = AdRequest.Builder()
            (pamManager?.getPAM(AdType.REWARDED, lastRevenue) as? PAM)?.let { pam ->
                val bundle = Bundle().apply {
                    val pamBundle = Bundle().apply { putString("userGroup", pam.value) }
                    putBundle("admob_custom_keyvals", pamBundle)
                }
                adRequest.addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
            }
            RewardedAd.load(App.Instance, adProperty.adUnit, adRequest.build(), object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    onAdLoadFailure("${p0.code};${p0.message}")
                }

                override fun onAdLoaded(p0: RewardedAd) {
                    super.onAdLoaded(p0)
                    rewardedAd = p0
                    rewardedAd?.onPaidEventListener = this@RewardedAdLoader
                    try {
                        p0.responseInfo.loadedAdapterResponseInfo?.let { resp ->
                            eventParams[EventParams.EVENT_PARAM_AD_MEDIATION] = resp.adSourceName
                            eventParams[EventParams.EVENT_PARAM_AD_SOURCE_INSTANCE] = resp.adSourceInstanceName
                        }

                        val extra = p0.responseInfo.responseExtras
                        eventParams[EventParams.EVENT_PARAM_AD_MEDIATION_GROUP] = extra.getString("mediation_group_name", "")
                        eventParams[EventParams.EVENT_PARAM_AD_MEDIATION_AB_TEST] = extra.getString("mediation_ab_test_name", "")
                    } catch (e: Exception) {
                        ILog.w(TAG, "video loaded success but response info err:${e.message}")
                    }
                    onAdLoadSuccess()
                }
            })
        } catch (e: Exception) {
            onAdLoadFailure("${e.message}")
        }
    }

    override fun show(activity: Activity, tag: String, placement: Int, clientInfo: String?) {
        super.show(activity, tag, placement, clientInfo)
        rewardedAd?.let { ad ->
            this@RewardedAdLoader.taskId = taskId
            ad.fullScreenContentCallback = this@RewardedAdLoader.fullScreenContentCallback
            ad.show(activity) {
                this@RewardedAdLoader.gotReward = true
                onUserRewarded()
            }
        } ?: onAdShowFailed("invalid ad")
    }

    override fun isReady(): Boolean = rewardedAd != null

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

    private val fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()
            onAdShowSuccess()
        }

        override fun onAdClicked() {
            super.onAdClicked()
            this@RewardedAdLoader.onAdClicked()
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            rewardedAd = null
            onAdClosed(false)
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            super.onAdFailedToShowFullScreenContent(p0)
            rewardedAd = null
            onAdShowFailed("${p0.code};${p0.message}")
        }

    }

}